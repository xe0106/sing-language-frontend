package com.example.myapplication.ui.call.video_call.sign

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUtteranceControllerTest {

    private val controller = SignUtteranceController {
        TEST_SESSION_ID
    }

    @Test
    fun `세 번째 연속 손 감지 시 보관한 세 프레임부터 전송한다`() {
        assertTrue(
            controller.onFrame(frame(1_000L, hand = true, marker = 1f))
                .isEmpty()
        )
        assertTrue(
            controller.onFrame(frame(1_067L, hand = true, marker = 2f))
                .isEmpty()
        )

        val outgoing = controller.onFrame(
            frame(1_134L, hand = true, marker = 3f)
        )

        assertEquals(SignUtterancePhase.ACTIVE, controller.phase)
        assertEquals(TEST_SESSION_ID, controller.activeSessionId)
        assertEquals(listOf(0L, 1L, 2L), outgoing.map { it.sequence })
        assertEquals(listOf(1f, 2f, 3f), outgoing.map { it.features[0] })
        assertEquals(
            listOf(1_000L, 1_067L, 1_134L),
            outgoing.map { it.timestampMs }
        )
    }

    @Test
    fun `세 프레임 전에 손 감지가 끊기면 후보를 버린다`() {
        controller.onFrame(frame(1_000L, hand = true))
        controller.onFrame(frame(1_067L, hand = true))

        val interrupted = controller.onFrame(
            frame(1_134L, hand = false)
        )
        val next = controller.onFrame(
            frame(1_201L, hand = true)
        )

        assertTrue(interrupted.isEmpty())
        assertTrue(next.isEmpty())
        assertEquals(SignUtterancePhase.CANDIDATE, controller.phase)
    }

    @Test
    fun `왼손 또는 오른손 하나만 감지되어도 발화를 시작한다`() {
        controller.onFrame(
            frame(1_000L, leftHand = true, rightHand = false)
        )
        controller.onFrame(
            frame(1_067L, leftHand = false, rightHand = true)
        )
        val outgoing = controller.onFrame(
            frame(1_134L, leftHand = true, rightHand = false)
        )

        assertEquals(3, outgoing.size)
        assertEquals(SignUtterancePhase.ACTIVE, controller.phase)
    }

    @Test
    fun `손 미감지 프레임을 일 초 동안 전송한 뒤 종료한다`() {
        startUtterance()

        val firstMissing = controller.onFrame(
            frame(1_200L, hand = false)
        )
        val occluded = controller.onFrame(
            frame(1_800L, hand = false)
        )
        val ending = controller.onFrame(
            frame(2_200L, hand = false)
        )

        assertEquals(3L, firstMissing.single().sequence)
        assertEquals(4L, occluded.single().sequence)
        assertEquals(5L, ending.single().sequence)
        assertEquals(SignUtterancePhase.NEUTRAL, controller.phase)
        assertEquals(null, controller.activeSessionId)
    }

    @Test
    fun `reset은 폐기한 활성 sessionId를 반환한다`() {
        startUtterance()

        val discardedSessionId = controller.reset()

        assertEquals(TEST_SESSION_ID, discardedSessionId)
        assertEquals(SignUtterancePhase.NEUTRAL, controller.phase)
        assertEquals(null, controller.activeSessionId)
    }

    @Test
    fun `발화 시작 오 초 후에는 프레임을 전송하지 않고 종료 대기한다`() {
        startUtterance()

        val beforeTimeout = controller.onFrame(
            frame(5_999L, hand = true)
        )
        val timedOut = controller.onFrame(
            frame(6_000L, hand = true)
        )

        assertEquals(1, beforeTimeout.size)
        assertTrue(timedOut.isEmpty())
        assertEquals(
            SignUtterancePhase.WAITING_NEUTRAL,
            controller.phase
        )
    }

    @Test
    fun `reset 후 다음 발화는 sequence 영부터 다시 시작한다`() {
        startUtterance()
        controller.onFrame(frame(1_200L, hand = true))

        controller.reset()

        controller.onFrame(frame(2_000L, hand = true))
        controller.onFrame(frame(2_067L, hand = true))
        val outgoing = controller.onFrame(
            frame(2_134L, hand = true)
        )

        assertEquals(listOf(0L, 1L, 2L), outgoing.map { it.sequence })
    }

    private fun startUtterance() {
        controller.onFrame(frame(1_000L, hand = true))
        controller.onFrame(frame(1_067L, hand = true))
        controller.onFrame(frame(1_134L, hand = true))
    }

    private fun frame(
        timestampMs: Long,
        hand: Boolean,
        marker: Float = 0f
    ): SignFeatureFrame = frame(
        timestampMs = timestampMs,
        leftHand = hand,
        rightHand = false,
        marker = marker
    )

    private fun frame(
        timestampMs: Long,
        leftHand: Boolean,
        rightHand: Boolean,
        marker: Float = 0f
    ): SignFeatureFrame =
        SignFeatureFrame(
            timestampMs = timestampMs,
            poseDetected = true,
            leftHandDetected = leftHand,
            rightHandDetected = rightHand,
            features = FloatArray(
                SignFeatureVectorBuilder.FEATURE_COUNT
            ).also { it[0] = marker }
        )

    private companion object {
        const val TEST_SESSION_ID =
            "b2683ee7-1435-4726-9e64-d07eb0c56613"
    }
}
