package com.example.myapplication.ui.call.video_call.sign

import java.util.UUID

/**
 * 연속 프레임을 한 개의 수어 발화로 묶고 서버 전송 순서를 결정한다.
 *
 * 네트워크 작업은 하지 않으며, [onFrame]이 반환한 프레임만 호출자가 순서대로
 * 전송하면 된다. 모든 함수는 동일한 스레드에서 호출해야 한다.
 */
class SignUtteranceController(
    private val sessionIdProvider: () -> String = {
        UUID.randomUUID().toString()
    }
) {
    var phase: SignUtterancePhase = SignUtterancePhase.NEUTRAL
        private set

    val activeSessionId: String?
        get() = activeUtterance?.sessionId

    private val candidateFrames =
        ArrayDeque<SignFeatureFrame>(START_FRAME_COUNT)

    private var activeUtterance: ActiveUtterance? = null
    private var neutralSinceMs: Long? = null

    fun onFrame(
        frame: SignFeatureFrame
    ): List<SignUtteranceFrame> {
        validateFrame(frame)

        return when (phase) {
            SignUtterancePhase.NEUTRAL,
            SignUtterancePhase.CANDIDATE ->
                handleCandidateFrame(frame)

            SignUtterancePhase.ACTIVE ->
                handleActiveFrame(frame)

            SignUtterancePhase.WAITING_NEUTRAL -> {
                handleWaitingNeutralFrame(frame)
                emptyList()
            }
        }
    }

    /**
     * 본인이 생성한 자막 중 현재 발화의 sessionId와 일치하는 경우에만 종료한다.
     * 늦은 자막 또는 sessionId가 없는 자막은 현재 상태에 영향을 주지 않는다.
     */
    fun onOwnSubtitle(sessionId: String?) {
        val utterance = activeUtterance ?: return

        if (
            phase != SignUtterancePhase.ACTIVE ||
            sessionId == null ||
            sessionId != utterance.sessionId
        ) {
            return
        }

        activeUtterance = null
        candidateFrames.clear()
        neutralSinceMs = utterance.noHandSinceMs
        phase = SignUtterancePhase.WAITING_NEUTRAL
    }

    /** 소켓 재연결 또는 통화 변경 시 진행 중인 발화를 폐기한다. */
    fun reset() {
        candidateFrames.clear()
        activeUtterance = null
        neutralSinceMs = null
        phase = SignUtterancePhase.NEUTRAL
    }

    private fun handleCandidateFrame(
        frame: SignFeatureFrame
    ): List<SignUtteranceFrame> {
        if (!frame.handDetected) {
            candidateFrames.clear()
            phase = SignUtterancePhase.NEUTRAL
            return emptyList()
        }

        candidateFrames.addLast(frame)
        phase = SignUtterancePhase.CANDIDATE

        if (candidateFrames.size < START_FRAME_COUNT) {
            return emptyList()
        }

        val sessionId = sessionIdProvider().also {
            require(it.isNotBlank()) {
                "발화 sessionId가 비어 있습니다."
            }
        }
        val startedAtMs = candidateFrames.first().timestampMs
        val outgoingFrames = candidateFrames
            .mapIndexed { index, candidate ->
                candidate.toOutgoingFrame(
                    sessionId = sessionId,
                    sequence = index.toLong()
                )
            }

        activeUtterance = ActiveUtterance(
            sessionId = sessionId,
            startedAtMs = startedAtMs,
            nextSequence = START_FRAME_COUNT.toLong(),
            noHandSinceMs = null
        )
        candidateFrames.clear()
        neutralSinceMs = null
        phase = SignUtterancePhase.ACTIVE

        return outgoingFrames
    }

    private fun handleActiveFrame(
        frame: SignFeatureFrame
    ): List<SignUtteranceFrame> {
        val utterance = checkNotNull(activeUtterance)

        if (
            frame.timestampMs - utterance.startedAtMs >=
            MAX_UTTERANCE_DURATION_MS
        ) {
            moveToWaitingNeutral(
                noHandSinceMs = utterance.noHandSinceMs,
                frame = frame
            )
            return emptyList()
        }

        val noHandSinceMs = when {
            frame.handDetected -> null
            utterance.noHandSinceMs != null ->
                utterance.noHandSinceMs
            else -> frame.timestampMs
        }
        val outgoingFrame = frame.toOutgoingFrame(
            sessionId = utterance.sessionId,
            sequence = utterance.nextSequence
        )

        activeUtterance = utterance.copy(
            nextSequence = utterance.nextSequence + 1L,
            noHandSinceMs = noHandSinceMs
        )

        if (
            noHandSinceMs != null &&
            frame.timestampMs - noHandSinceMs >=
            NEUTRAL_DURATION_MS
        ) {
            finishWithConfirmedNeutral()
        }

        return listOf(outgoingFrame)
    }

    private fun moveToWaitingNeutral(
        noHandSinceMs: Long?,
        frame: SignFeatureFrame
    ) {
        activeUtterance = null
        candidateFrames.clear()

        neutralSinceMs = when {
            frame.handDetected -> null
            noHandSinceMs != null -> noHandSinceMs
            else -> frame.timestampMs
        }
        phase = SignUtterancePhase.WAITING_NEUTRAL

        val currentNeutralSinceMs = neutralSinceMs
        if (
            currentNeutralSinceMs != null &&
            frame.timestampMs - currentNeutralSinceMs >=
            NEUTRAL_DURATION_MS
        ) {
            finishWithConfirmedNeutral()
        }
    }

    private fun handleWaitingNeutralFrame(
        frame: SignFeatureFrame
    ) {
        if (frame.handDetected) {
            neutralSinceMs = null
            return
        }

        val startedAtMs = neutralSinceMs
            ?: frame.timestampMs.also {
                neutralSinceMs = it
            }

        if (
            frame.timestampMs - startedAtMs >=
            NEUTRAL_DURATION_MS
        ) {
            finishWithConfirmedNeutral()
        }
    }

    private fun finishWithConfirmedNeutral() {
        candidateFrames.clear()
        activeUtterance = null
        neutralSinceMs = null
        phase = SignUtterancePhase.NEUTRAL
    }

    private fun SignFeatureFrame.toOutgoingFrame(
        sessionId: String,
        sequence: Long
    ): SignUtteranceFrame =
        SignUtteranceFrame(
            sessionId = sessionId,
            sequence = sequence,
            timestampMs = timestampMs,
            poseDetected = poseDetected,
            leftHandDetected = leftHandDetected,
            rightHandDetected = rightHandDetected,
            features = features.copyOf()
        )

    private fun validateFrame(frame: SignFeatureFrame) {
        require(
            frame.features.size ==
            SignFeatureVectorBuilder.FEATURE_COUNT
        ) {
            "수어 특징은 정확히 258개여야 합니다."
        }
        require(frame.features.all(Float::isFinite)) {
            "수어 특징에는 NaN 또는 Infinity를 사용할 수 없습니다."
        }
    }

    private data class ActiveUtterance(
        val sessionId: String,
        val startedAtMs: Long,
        val nextSequence: Long,
        val noHandSinceMs: Long?
    )

    private companion object {
        const val START_FRAME_COUNT = 3
        const val NEUTRAL_DURATION_MS = 1_000L
        const val MAX_UTTERANCE_DURATION_MS = 5_000L
    }
}

enum class SignUtterancePhase {
    NEUTRAL,
    CANDIDATE,
    ACTIVE,
    WAITING_NEUTRAL
}

data class SignUtteranceFrame(
    val sessionId: String,
    val sequence: Long,
    val timestampMs: Long,
    val poseDetected: Boolean,
    val leftHandDetected: Boolean,
    val rightHandDetected: Boolean,
    val features: FloatArray
)
