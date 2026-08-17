package com.example.myapplication.dto

enum class IncomingCallNotificationType {
    INCOMING_CALL
}

data class IncomingCallNotificationDto(
    val type: IncomingCallNotificationType,
    val callId: String,
    val callerId: Long,
    val receiverId: Long,
    val status: String
)