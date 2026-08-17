package com.example.myapplication.ui.call.video_call

sealed interface CallSignal {
    val callId: String
    val senderId: Long
    val receiverId: Long?

    data class Offer(
        override val callId: String,
        override val senderId: Long,
        override val receiverId: Long?,
        val sdp: String
    ): CallSignal

    data class Answer(
        override val callId: String,
        override val senderId: Long,
        override val receiverId: Long?,
        val sdp: String
    ) : CallSignal

    data class IceCandidate(
        override val callId: String,
        override val senderId: Long,
        override val receiverId: Long?,
        val candidate: String,
        val sdpMid: String?,
        val sdpMLineIndex: Int
    ) : CallSignal
}