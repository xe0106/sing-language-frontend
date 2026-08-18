package com.example.myapplication.dto

import com.google.gson.JsonElement

enum class CallSocketMessageType {
    JOIN,
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    SUBTITLE,
    LEAVE,
    CALL_STATUS_CHANGE
}

data class CallSocketMessageDto(
    val type: CallSocketMessageType,
    val callId: String,
    val senderId: Long? = null,
    val receiverId: Long? = null,
    val callerId: Long? = null,
    val status: CallStatus? = null,
    val data: JsonElement? = null,
    val textContent: String? = null,
    val subtitleId: Long? = null,
    val createdAt: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null
)

data class SdpDataDto(
    val type: String,
    val sdp: String
)

data class IceCandidateDataDto(
    val candidate: String,
    val sdpMid: String?,
    val sdpMLineIndex: Int
)