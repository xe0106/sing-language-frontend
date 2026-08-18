package com.example.myapplication.ui.call.video_call

data class VideoCallSession(
    val callId: String,
    val remoteUserId: Long?,
    val remoteName: String,
    val isOutgoing: Boolean
)
