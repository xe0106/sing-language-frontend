package com.example.myapplication.ui.call.video_call

data class VideoCallUiState(
    val callId: String? = null,
    val remoteName: String = "",
    val connectionState: CallConnectionState = CallConnectionState.CONNECTING,
    val isLocalVideoReady: Boolean = false,
    val isRemoteVideoReady: Boolean = false,
    val isMicEnabled: Boolean = true,
    val messages: List<CallMessage> = emptyList(),
    val messageInput: String = "",
    val errorMessage: String? = null
)

data class CallMessage(
    val id: Long,
    val text: String,
    val isMine: Boolean
)