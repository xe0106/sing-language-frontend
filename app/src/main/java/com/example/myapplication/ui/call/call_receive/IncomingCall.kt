package com.example.myapplication.ui.call.call_receive

data class IncomingCall(
    val callId: String,
    val callerName: String,
    val callerProfileImageUrl: String?
)