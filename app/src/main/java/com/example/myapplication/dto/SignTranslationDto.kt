package com.example.myapplication.dto

data class LandmarkFramePayload(
    val type: String = TYPE_LANDMARK_FRAME,
    val sessionId: String,
    val callId: String,
    val senderId: Long,
    val sequence: Long,
    val timestampMs: Long,
    val features: FloatArray
) {
    companion object {
        const val TYPE_LANDMARK_FRAME = "landmark_frame"
    }
}

data class SignSessionEndPayload(
    val type: String = TYPE_SESSION_END,
    val callId: String,
    val sessionId: String,
    val senderId: Long,
    val timestampMs: Long
) {
    companion object {
        const val TYPE_SESSION_END = "session_end"
    }
}
