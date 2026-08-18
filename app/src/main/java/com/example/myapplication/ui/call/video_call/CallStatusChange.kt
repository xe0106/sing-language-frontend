package com.example.myapplication.ui.call.video_call

import com.example.myapplication.dto.CallStatus

data class CallStatusChange(
    val callId: String,
    val callerId: Long,
    val receiverId: Long,
    val status: CallStatus,
    val startedAt: String?,
    val endedAt: String?
)