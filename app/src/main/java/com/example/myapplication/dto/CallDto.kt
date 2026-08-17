package com.example.myapplication.dto

data class ContactResponse(
    val contactId: Long,
    val targetUserId: Long,
    val contactName: String,
    val profileImageUrl: String,
    val createdAt: String,
    val lastContactedAt: String
)

data class ContactInsertRequest(
    val phoneNumber: String,
    val contactName: String,
    val profileImageUrl: String? = null
)

data class ContactInsertResponse(
    val contactId: Long,
    val userId: Long,
    val targetUserId: Long,
    val contactName: String,
    val createdAt: String
)

data class CallOutRequest(
    val callerId: Long,
    val receiverId: Long
)

data class CallOutResponse(
    val callId: String,
    val callerId: Long,
    val receiverId: Long,
    val status: String,
    val startedAt: String,
    val endedAt: String?
)

data class SubtitleResponse(
    val subtitleId: Long,
    val callId: String,
    val senderId: Long,
    val textContent: String,
    val createdAt: String
)

enum class CallStatus {
    CONNECTED,
    ENDED,
    REJECTED
}

data class UpdateCallStatusRequest(
    val status: CallStatus
)

data class UpdateCallStatusResponse(
    val type: CallSocketMessageType,
    val callId: String,
    val callerId: Long,
    val receiverId: Long,
    val status: CallStatus,
    val startedAt: String?,
    val endedAt: String?
)