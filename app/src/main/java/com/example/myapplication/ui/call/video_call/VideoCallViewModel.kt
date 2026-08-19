package com.example.myapplication.ui.call.video_call

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.dto.CallStatus
import com.example.myapplication.network.call.CallSocketConnectionState
import com.example.myapplication.network.call.webrtc.IceServerConfig
import com.example.myapplication.network.call.webrtc.WebRtcClient
import com.example.myapplication.network.call.webrtc.WebRtcEvent
import com.example.myapplication.ui.call.CallRepository
import com.example.myapplication.ui.call.video_call.sign.SignFeatureFrame
import com.example.myapplication.ui.call.video_call.sign.SignUtteranceController
import com.example.myapplication.ui.call.video_call.sign.SignUtteranceFrame
import com.example.myapplication.ui.call.video_call.sign.SignUtterancePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val webRtcClient: WebRtcClient
) : ViewModel() {
    var uiState by mutableStateOf(VideoCallUiState())
        private set

    val eglBaseContext =
        webRtcClient.eglBaseContext

    val localVideoTrack =
        webRtcClient.localVideoTrack

    val remoteVideoTrack =
        webRtcClient.remoteVideoTrack

    private var remoteUserId: Long? = null
    private var loadedCallId: String? = null

    private var isOutgoingCall: Boolean? = null

    private var hasReceivedConnectedStatus = false

    private var hasRemoteJoined = false

    private var hasSentInitialOffer = false

    private val signUtteranceController =
        SignUtteranceController()

    private var signSessionDiagnostics: SignSessionDiagnostics? = null

    private val iceServers =
        listOf(
            IceServerConfig(
                urls =
                    listOf(
                        "stun:stun.l.google.com:19302"
                    )
            )
        )

    private var isEndingCall = false

    private val cleanupScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    init {
        viewModelScope.launch {
            webRtcClient.localVideoTrack.collect { track ->
                uiState = uiState.copy(
                    isLocalVideoReady = track != null
                )
            }
        }

        viewModelScope.launch {
            webRtcClient.remoteVideoTrack.collect { track ->
                uiState = uiState.copy(
                    isRemoteVideoReady = track != null
                )
            }
        }

        viewModelScope.launch {
            webRtcClient.localSignFeatures.collect { featureFrame ->
                processSignFeatureFrame(featureFrame)
            }
        }

        viewModelScope.launch {
            webRtcClient.events.collect { event ->
                when (event) {
                    is WebRtcEvent.LocalIceCandidate -> {
                        sendLocalIceCandidate(event)
                    }

                    WebRtcEvent.Connected -> {
                        if (
                            uiState.connectionState !=
                            CallConnectionState.ENDED
                        ) {
                            uiState = uiState.copy(
                                connectionState =
                                    CallConnectionState.CONNECTED,
                                errorMessage = null
                            )
                        }
                    }

                    WebRtcEvent.Disconnected -> {
                        // 일시적인 네트워크 단절일 수 있으므로
                        // 아직 통화를 종료하지 않는다.
                    }

                    is WebRtcEvent.Failed -> {
                        if (
                            uiState.connectionState !=
                            CallConnectionState.ENDED
                        ) {
                            uiState.callId?.let { callId ->
                                callRepository.disconnectCallSocket(callId)
                            }
                            webRtcClient.close()

                            uiState = uiState.copy(
                                connectionState =
                                    CallConnectionState.FAILED,
                                errorMessage =
                                    event.reason
                                        ?: "영상 연결에 실패했습니다."
                            )
                        }
                    }

                    is WebRtcEvent.MediaOperationFailed -> {
                        uiState = uiState.copy(
                            errorMessage = event.reason
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            callRepository.subtitleMessages.collect { subtitle ->
                Log.d(
                    LOG_TAG,
                    "Sign subtitle RECEIVE sessionId=${subtitle.sessionId}, " +
                        "text=${subtitle.text}, isMine=${subtitle.isMine}"
                )

                if (uiState.messages.none {it.id == subtitle.id}) {
                    uiState = uiState.copy(
                        messages = uiState.messages + subtitle
                    )
                }

                if (subtitle.isMine) {
                    val activeSessionId =
                        signUtteranceController.activeSessionId
                    signUtteranceController.onOwnSubtitle(
                        subtitle.sessionId
                    )
                    if (
                        activeSessionId != null &&
                        subtitle.sessionId == activeSessionId
                    ) {
                        finishSignDiagnostics(
                            sessionId = activeSessionId,
                            reason = "SUBTITLE"
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            callRepository.remoteJoinCallIds.collect { joinedCallId ->
                if (uiState.callId != joinedCallId) {
                    return@collect
                }

                hasRemoteJoined = true

                sendInitialOffer(joinedCallId)
            }
        }

        viewModelScope.launch {
            callRepository.remoteLeaveCallIds.collect { endedCallId ->
                if (uiState.callId != endedCallId) {
                    return@collect
                }

                finishRemoteCall(endedCallId)
            }
        }

        viewModelScope.launch {
            callRepository.callStatusChanges.collect { statusChange ->
                if (uiState.callId != statusChange.callId) {
                    return@collect
                }

                // 종료 이후 늦게 도착한 상태 메시지는 무시
                if (
                    uiState.connectionState ==
                    CallConnectionState.ENDED
                ) {
                    return@collect
                }

                when (statusChange.status) {
                    CallStatus.CONNECTED -> {
                        hasReceivedConnectedStatus = true

                        uiState = uiState.copy(
                            connectionState =
                                CallConnectionState.CONNECTED,
                            errorMessage = null
                        )

                        sendInitialOffer(
                            statusChange.callId
                        )
                    }

                    CallStatus.REJECTED -> {
                        finishRemoteCall(
                            callId = statusChange.callId,
                            message = "상대방이 통화를 거절했습니다."
                        )
                    }

                    CallStatus.ENDED -> {
                        finishRemoteCall(
                            callId = statusChange.callId
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            callRepository.callSocketConnectionState.collect { socketState ->
                if (socketState != CallSocketConnectionState.CONNECTED) {
                    // 재연결되더라도 이전 AI 세션을 이어서 보내지 않는다.
                    signUtteranceController.reset()
                }

                if (
                    socketState != CallSocketConnectionState.FAILED ||
                    uiState.connectionState == CallConnectionState.ENDED
                ) {
                    return@collect
                }

                webRtcClient.close()

                loadedCallId = null
                remoteUserId = null

                uiState = uiState.copy(
                    connectionState = CallConnectionState.FAILED,
                    isLocalVideoReady = false,
                    isRemoteVideoReady = false,
                    errorMessage = "통화 서버와의 연결이 끊어졌습니다."
                )
            }
        }

        viewModelScope.launch {
            callRepository.remoteCallSignals.collect { signal ->
                if (
                    signal.callId != uiState.callId ||
                    uiState.connectionState ==
                    CallConnectionState.ENDED ||
                    uiState.connectionState ==
                    CallConnectionState.FAILED
                ) {
                    return@collect
                }

                handleRemoteCallSignal(signal)
            }
        }
    }

    private var isFinishingRemoteCall = false

    private suspend fun processSignFeatureFrame(
        featureFrame: SignFeatureFrame
    ) {
        val callId = uiState.callId

        if (
            callId == null ||
            callRepository.callSocketConnectionState.value !=
            CallSocketConnectionState.CONNECTED ||
            uiState.connectionState != CallConnectionState.CONNECTED
        ) {
            signUtteranceController.reset()
            return
        }

        val previousPhase = signUtteranceController.phase
        val previousSessionId =
            signUtteranceController.activeSessionId

        val outgoingFrames = runCatching {
            signUtteranceController.onFrame(featureFrame)
        }.getOrElse { throwable ->
            Log.e(
                LOG_TAG,
                "Invalid sign feature frame",
                throwable
            )
            signUtteranceController.reset()
            return
        }

        val currentPhase = signUtteranceController.phase
        val currentSessionId =
            signUtteranceController.activeSessionId

        if (
            previousPhase != SignUtterancePhase.ACTIVE &&
            currentPhase == SignUtterancePhase.ACTIVE &&
            currentSessionId != null
        ) {
            signSessionDiagnostics = SignSessionDiagnostics(
                sessionId = currentSessionId
            )
            Log.d(
                LOG_TAG,
                "Sign utterance START sessionId=$currentSessionId, " +
                    "bufferedFrames=${outgoingFrames.size}"
            )
        }

        for (frame in outgoingFrames) {
            val result = runCatching {
                callRepository.sendSignFeatures(
                    callId = callId,
                    sessionId = frame.sessionId,
                    sequence = frame.sequence,
                    timestampMs = frame.timestampMs,
                    features = frame.features
                )
            }

            if (result.isFailure) {
                // 같은 sequence를 재전송하지 않고 다음 연결에서 새 발화를 시작한다.
                signUtteranceController.reset()
                Log.w(
                    LOG_TAG,
                    "Failed to send sign feature frame",
                    result.exceptionOrNull()
                )
                return
            }

            recordSentSignFrame(frame)
        }

        if (
            previousSessionId != null &&
            currentSessionId == null
        ) {
            val reason = when (currentPhase) {
                SignUtterancePhase.NEUTRAL ->
                    "NO_HAND_1000_MS"
                SignUtterancePhase.WAITING_NEUTRAL ->
                    "MAX_DURATION_5000_MS"
                else -> "STATE_CHANGE"
            }
            finishSignDiagnostics(
                sessionId = previousSessionId,
                reason = reason
            )
        }
    }

    private fun recordSentSignFrame(
        frame: SignUtteranceFrame
    ) {
        val diagnostics = signSessionDiagnostics
            ?.takeIf { it.sessionId == frame.sessionId }

        diagnostics?.record(frame)

        Log.d(
            LOG_TAG,
            "Sign frame SEND sessionId=${frame.sessionId}, " +
                "sequence=${frame.sequence}, " +
                "pose=${frame.poseDetected}, " +
                "left=${frame.leftHandDetected}, " +
                "right=${frame.rightHandDetected}"
        )
    }

    private fun finishSignDiagnostics(
        sessionId: String,
        reason: String
    ) {
        val diagnostics = signSessionDiagnostics
            ?.takeIf { it.sessionId == sessionId }

        if (diagnostics == null) {
            Log.d(
                LOG_TAG,
                "Sign utterance END sessionId=$sessionId, " +
                    "reason=$reason, stats=unavailable"
            )
            return
        }

        Log.d(
            LOG_TAG,
            "Sign utterance END sessionId=$sessionId, " +
                "reason=$reason, " +
                "totalFrames=${diagnostics.totalFrames}, " +
                "poseFrames=${diagnostics.poseFrames}, " +
                "leftHandFrames=${diagnostics.leftHandFrames}, " +
                "rightHandFrames=${diagnostics.rightHandFrames}, " +
                "bothHandFrames=${diagnostics.bothHandFrames}, " +
                "noHandFrames=${diagnostics.noHandFrames}"
        )
        signSessionDiagnostics = null
    }

    private suspend fun finishRemoteCall(
        callId: String,
        message: String? = null
    ) {
        if (
            uiState.callId != callId ||
            uiState.connectionState ==
            CallConnectionState.ENDED ||
            isFinishingRemoteCall
        ) {
            return
        }

        isFinishingRemoteCall = true
        signUtteranceController.reset()

        callRepository.handleRemoteCallEnded(callId)
        webRtcClient.close()

        loadedCallId = null
        remoteUserId = null

        uiState = uiState.copy(
            connectionState = CallConnectionState.ENDED,
            isLocalVideoReady = false,
            isRemoteVideoReady = false,
            errorMessage = message
        )
    }

    private suspend fun sendLocalIceCandidate(
        event: WebRtcEvent.LocalIceCandidate
    ) {
        val callId = uiState.callId ?: return

        if (
            uiState.connectionState ==
            CallConnectionState.ENDED
        ) {
            return
        }

        runCatching {
            callRepository.sendIceCandidate(
                callId = callId,
                receiverId = remoteUserId,
                candidate = event.candidate,
                sdpMid = event.sdpMid,
                sdpMLineIndex = event.sdpMLineIndex
            )
        }.onFailure { exception ->
            uiState = uiState.copy(
                errorMessage =
                    exception.message
                        ?: "네트워크 연결 정보를 전송하지 못했습니다."
            )
        }
    }

    private suspend fun handleRemoteCallSignal(
        signal: CallSignal
    ) {
        runCatching {
            when (signal) {
                is CallSignal.Offer -> {
                    remoteUserId = signal.senderId

                    webRtcClient.setRemoteOffer(
                        signal.sdp
                    )

                    val answer =
                        webRtcClient.createAnswer()

                    callRepository.sendAnswer(
                        callId = signal.callId,
                        receiverId = signal.senderId,
                        sdp = answer
                    )
                }

                is CallSignal.Answer -> {
                    webRtcClient.setRemoteAnswer(
                        signal.sdp
                    )
                }

                is CallSignal.IceCandidate -> {
                    webRtcClient.addRemoteIceCandidate(
                        candidate = signal.candidate,
                        sdpMid = signal.sdpMid,
                        sdpMLineIndex =
                            signal.sdpMLineIndex
                    )
                }
            }
        }.onFailure { exception ->
            failVideoConnection(exception)
        }
    }

    private suspend fun failVideoConnection(
        exception: Throwable
    ) {
        signUtteranceController.reset()

        runCatching {
            uiState.callId?.let { callId ->
                callRepository.disconnectCallSocket(callId)
            }
        }

        runCatching {
            webRtcClient.close()
        }

        loadedCallId = null
        remoteUserId = null
        isOutgoingCall = null

        uiState = uiState.copy(
            connectionState =
                CallConnectionState.FAILED,
            isLocalVideoReady = false,
            isRemoteVideoReady = false,
            errorMessage =
                exception.message
                    ?: "영상 연결에 실패했습니다."
        )
    }

    private suspend fun sendInitialOffer(
        callId: String
    ) {
        if (
            isOutgoingCall != true ||
            !hasReceivedConnectedStatus ||
            !hasRemoteJoined ||
            hasSentInitialOffer ||
            uiState.connectionState ==
            CallConnectionState.ENDED ||
            uiState.connectionState ==
            CallConnectionState.FAILED
        ) {
            return
        }

        hasSentInitialOffer = true

        runCatching {
            val offer =
                webRtcClient.createOffer()

            callRepository.sendOffer(
                callId = callId,
                receiverId = remoteUserId,
                sdp = offer
            )
        }.onFailure { exception ->
            failVideoConnection(exception)
        }
    }

    fun loadCall(callId: String) {
        if(loadedCallId == callId) return

        signUtteranceController.reset()
        loadedCallId = callId

        isOutgoingCall = null
        hasReceivedConnectedStatus = false
        hasRemoteJoined = false
        hasSentInitialOffer = false

        loadSubtitles(callId)

        viewModelScope.launch{
            uiState=uiState.copy(
                callId=callId,
                connectionState = CallConnectionState.CONNECTING,
                errorMessage = null
            )

            runCatching {
                val session = callRepository.getVideoCallSession(callId)

                remoteUserId = session.remoteUserId
                isOutgoingCall = session.isOutgoing

                uiState=uiState.copy(
                    remoteName = session.remoteName,
                    connectionState = if(session.isOutgoing) {
                        CallConnectionState.CALLING
                    } else {
                        CallConnectionState.CONNECTING
                    }
                )

                // 카메라·마이크·PeerConnection 생성
                webRtcClient.start(
                    iceServers = iceServers
                )

                // 통화방 STOMP 연결
                callRepository.connectCallSocket(
                    callId = session.callId,
                    receiverId = session.remoteUserId
                )

                sendInitialOffer(session.callId)

                callRepository.connectVideoCall(callId)

                session
            }.onSuccess { session ->
                if (
                    uiState.connectionState ==
                    CallConnectionState.ENDED
                ) {
                    return@onSuccess
                }

                val isAlreadyConnected =
                    uiState.connectionState ==
                            CallConnectionState.CONNECTED

                uiState=uiState.copy(
                    connectionState =
                        if(
                            session.isOutgoing &&
                            !isAlreadyConnected
                        ) {
                            CallConnectionState.CALLING
                        } else {
                            CallConnectionState.CONNECTED
                        },
                    isLocalVideoReady =
                        webRtcClient.localVideoTrack.value != null,
                    isRemoteVideoReady =
                        webRtcClient.remoteVideoTrack.value != null
                )
            }.onFailure { exception ->
                callRepository.disconnectCallSocket(callId)
                webRtcClient.close()

                loadedCallId = null
                remoteUserId = null

                uiState=uiState.copy(
                    connectionState = CallConnectionState.FAILED,
                    isLocalVideoReady = false,
                    isRemoteVideoReady = false,
                    errorMessage =
                        exception.message
                            ?: "영상 통화에 연결하지 못했습니다."
                )
            }
        }
    }

    private fun loadSubtitles(callId: String) {
        viewModelScope.launch {
            runCatching {
                callRepository.getSubtitles(callId)
            }.onSuccess { subtitles ->
                uiState = uiState.copy(
                    messages = (subtitles + uiState.messages).distinctBy { it.id }
                )
            }.onFailure { exception ->
                uiState = uiState.copy(
                    errorMessage = exception.message ?: "자막 목록을 불러오지 못했습니다."
                )
            }
        }
    }

    fun updateMessage(value: String) {
        uiState=uiState.copy(messageInput = value)
    }

    fun sendMessage(){
        val callId = uiState.callId ?: return
        val message = uiState.messageInput.trim()

        if(message.isEmpty()) return

        viewModelScope.launch{
            runCatching {
                callRepository.sendSubtitle(
                    callId = callId,
                    receiverId = remoteUserId,
                    text = message
                )
            }.onSuccess {
                uiState=uiState.copy(
                    messageInput = ""
                )
            }.onFailure { exception ->
                uiState=uiState.copy(
                    errorMessage =
                        exception.message?: "메시지를 전송하지 못했습니다."
                )
            }
        }
    }

    fun toggleMic(){
        val enabled = !uiState.isMicEnabled

        webRtcClient.setMicrophoneEnabled(
            enabled
        )

        uiState=uiState.copy(
            isMicEnabled = enabled
        )
    }

    fun switchCamera() {
        if (!uiState.isLocalVideoReady) {
            return
        }

        webRtcClient.switchCamera()
    }

    fun endCall() {
        val callId = uiState.callId ?: return

        if (
            isEndingCall ||
            uiState.connectionState ==
            CallConnectionState.ENDED
        ) {
            return
        }

        isEndingCall = true
        signUtteranceController.reset()

        viewModelScope.launch{
            val result =
                runCatching {
                    callRepository.endVideoCall(callId)
                }

            // 종료 API의 성공 여부와 관계없이
            // 로컬 카메라·마이크는 반드시 정리
            runCatching {
                webRtcClient.close()
            }

            result.onSuccess {
                uiState = uiState.copy(
                    connectionState =
                        CallConnectionState.ENDED
                )
            }.onFailure { exception ->
                // 저장소도 finally에서 소켓을 닫으므로
                // 로컬에서는 통화가 끝난 상태로 처리
                uiState = uiState.copy(
                    connectionState =
                        CallConnectionState.ENDED,
                    errorMessage =
                        exception.message
                            ?: "통화 종료 상태를 서버에 전달하지 못했습니다."
                )
            }
        }
    }

    fun clearError(){
        uiState=uiState.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()

        signUtteranceController.reset()

        val callId = uiState.callId

        cleanupScope.launch {
            runCatching {
                if (callId != null) {
                    callRepository.disconnectCallSocket(callId)
                }
            }

            runCatching {
                webRtcClient.close()
            }
        }.invokeOnCompletion {
            cleanupScope.cancel()
        }
    }

    private companion object {
        const val LOG_TAG = "VideoCallViewModel"
    }

    private data class SignSessionDiagnostics(
        val sessionId: String,
        var totalFrames: Int = 0,
        var poseFrames: Int = 0,
        var leftHandFrames: Int = 0,
        var rightHandFrames: Int = 0,
        var bothHandFrames: Int = 0,
        var noHandFrames: Int = 0
    ) {
        fun record(frame: SignUtteranceFrame) {
            totalFrames += 1
            if (frame.poseDetected) poseFrames += 1
            if (frame.leftHandDetected) leftHandFrames += 1
            if (frame.rightHandDetected) rightHandFrames += 1
            if (
                frame.leftHandDetected &&
                frame.rightHandDetected
            ) {
                bothHandFrames += 1
            }
            if (
                !frame.leftHandDetected &&
                !frame.rightHandDetected
            ) {
                noHandFrames += 1
            }
        }
    }
}
