package com.example.myapplication.ui.call.video_call

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private var loadedCallId: String? = null

    fun loadCall(callId: String) {
        if(loadedCallId == callId) return
        loadedCallId = callId

        viewModelScope.launch{
            uiState=uiState.copy(
                callId=callId,
                connectionState = CallConnectionState.CONNECTING,
                errorMessage = null
            )

            runCatching {
                val session = callRepository.getVideoCallSession(callId)

                uiState=uiState.copy(
                    remoteName = session.remoteName,
                    connectionState = if(session.isOutgoing) {
                        CallConnectionState.CALLING
                    } else {
                        CallConnectionState.CONNECTING
                    }
                )

                callRepository.connectVideoCall(callId)
            }.onSuccess {
                uiState=uiState.copy(
                    connectionState = CallConnectionState.CONNECTED,
                    isLocalVideoReady = true,
                    isRemoteVideoReady = true
                )
            }.onFailure {
                uiState=uiState.copy(
                    connectionState = CallConnectionState.FAILED,
                    errorMessage = "영상 통화에 연결하지 못했습니다."
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
                callRepository.sendCallMessage(callId, message)
            }.onSuccess { sentMessage ->
                uiState=uiState.copy(
                    messages = uiState.messages + sentMessage,
                    messageInput = ""
                )
            }.onFailure {
                uiState=uiState.copy(
                    errorMessage = "메시지를 전송하지 못했습니다."
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