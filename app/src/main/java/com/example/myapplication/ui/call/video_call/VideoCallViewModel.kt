package com.example.myapplication.ui.call.video_call

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.dto.CallStatus
import com.example.myapplication.network.call.CallSocketConnectionState
import com.example.myapplication.ui.call.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {
    var uiState by mutableStateOf(VideoCallUiState())
        private set

    private var remoteUserId: Long? = null
    private var loadedCallId: String? = null

    init {
        viewModelScope.launch {
            callRepository.subtitleMessages.collect { subtitle ->
                if (uiState.messages.none {it.id == subtitle.id}) {
                    uiState = uiState.copy(
                        messages = uiState.messages + subtitle
                    )
                }
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
                        uiState = uiState.copy(
                            connectionState =
                                CallConnectionState.CONNECTED,
                            isLocalVideoReady = true,
                            isRemoteVideoReady = true,
                            errorMessage = null
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
                if (
                    socketState != CallSocketConnectionState.FAILED ||
                    uiState.connectionState == CallConnectionState.ENDED
                ) {
                    return@collect
                }

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
    }

    private var isFinishingRemoteCall = false

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

        callRepository.handleRemoteCallEnded(callId)

        loadedCallId = null
        remoteUserId = null

        uiState = uiState.copy(
            connectionState = CallConnectionState.ENDED,
            isLocalVideoReady = false,
            isRemoteVideoReady = false,
            errorMessage = message
        )
    }

    fun loadCall(callId: String) {
        if(loadedCallId == callId) return
        loadedCallId = callId

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

                uiState=uiState.copy(
                    remoteName = session.remoteName,
                    connectionState = if(session.isOutgoing) {
                        CallConnectionState.CALLING
                    } else {
                        CallConnectionState.CONNECTING
                    }
                )

                callRepository.connectCallSocket(
                    callId = session.callId,
                    receiverId = session.remoteUserId
                )

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
                    isLocalVideoReady = true,
                    isRemoteVideoReady =
                        !session.isOutgoing ||
                            isAlreadyConnected
                )
            }.onFailure { exception ->
                callRepository.disconnectCallSocket()

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
        uiState=uiState.copy(
            isMicEnabled = !uiState.isMicEnabled
        )
    }

    fun endCall() {
        val callId = uiState.callId ?: return

        viewModelScope.launch{
            runCatching {
                callRepository.endVideoCall(callId)
            }.onSuccess {
                uiState=uiState.copy(
                    connectionState = CallConnectionState.ENDED
                )
            }.onFailure {
                uiState=uiState.copy(
                    errorMessage = "통화를 종료하지 못했습니다."
                )
            }
        }
    }

    fun clearError(){
        uiState=uiState.copy(errorMessage = null)
    }
}