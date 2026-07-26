package com.example.myapplication.ui.call.call_receive

data class IncomingCall(
    val callId: Long,
    val callerName: String,
    val callerProfileImageUrl: String?
)