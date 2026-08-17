package com.example.myapplication.dto

import com.google.gson.JsonObject

enum class CallSocketMessageType {
    JOIN,
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    SUBTITLE,
    LEAVE
}

data class CallSocketMessageDto(
    val type: CallSocketMessageType,
    val callId: String,
    val senderId: Long,
    val receiverId: Long? = null,
    val data: JsonObject? = null,
    val textContent: String? = null,
    val subtitleId: Long? = null,
    val createdAt: String? = null
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