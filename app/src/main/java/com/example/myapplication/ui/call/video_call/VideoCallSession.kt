package com.example.myapplication.ui.call.video_call

data class VideoCallSession(
    val callId: Long,
    val remoteName: String,
    val isOutgoing: Boolean
)
