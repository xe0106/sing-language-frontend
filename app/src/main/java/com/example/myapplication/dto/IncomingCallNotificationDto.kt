package com.example.myapplication.dto

enum class IncomingCallNotificationType {
    INCOMING_CALL
}

data class IncomingCallNotificationDto(
    val type: IncomingCallNotificationType,
    val callId: String,
    val callerId: Long,
    val callerName: String? = null,
    val callerNickname: String? = null,
    val callerProfileImageUrl: String? = null,
    val receiverId: Long,
    val status: String,
    val startedAt: String? = null,
    val endedAt: String? = null
)