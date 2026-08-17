package com.example.myapplication.ui.call.video_call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType
import com.example.myapplication.dto.IceCandidateDataDto
import com.example.myapplication.dto.SdpDataDto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallSignalMapperTest {

    private val gson = Gson()

    @Test
    fun `OFFER 메시지를 Offer 시그널로 변환한다`() {
        val message = CallSocketMessageDto(
            type = CallSocketMessageType.OFFER,
            callId = "call-123",
            senderId = 1L,
            receiverId = 2L,
            data = gson.toJsonTree(
                SdpDataDto(
                    type = "offer",
                    sdp = "offer-sdp"
                )
            ).asJsonObject
        )

        val result = message.toCallSignal(gson)

        assertEquals(
            CallSignal.Offer(
                callId = "call-123",
                senderId = 1L,
                receiverId = 2L,
                sdp = "offer-sdp"
            ),
            result
        )
    }

    @Test
    fun `ANSWER 메시지를 Answer 시그널로 변환한다`() {
        val message = CallSocketMessageDto(
            type = CallSocketMessageType.ANSWER,
            callId = "call-123",
            senderId = 2L,
            receiverId = 1L,
            data = gson.toJsonTree(
                SdpDataDto(
                    type = "answer",
                    sdp = "answer-sdp"
                )
            ).asJsonObject
        )

        val result = message.toCallSignal(gson)

        assertEquals(
            CallSignal.Answer(
                callId = "call-123",
                senderId = 2L,
                receiverId = 1L,
                sdp = "answer-sdp"
            ),
            result
        )
    }

    @Test
    fun `ICE 메시지를 IceCandidate 시그널로 변환한다`() {
        val message = CallSocketMessageDto(
            type = CallSocketMessageType.ICE_CANDIDATE,
            callId = "call-123",
            senderId = 1L,
            receiverId = 2L,
            data = gson.toJsonTree(
                IceCandidateDataDto(
                    candidate = "candidate-value",
                    sdpMid = "0",
                    sdpMLineIndex = 0
                )
            ).asJsonObject
        )

        val result = message.toCallSignal(gson)

        assertEquals(
            CallSignal.IceCandidate(
                callId = "call-123",
                senderId = 1L,
                receiverId = 2L,
                candidate = "candidate-value",
                sdpMid = "0",
                sdpMLineIndex = 0
            ),
            result
        )
    }

    @Test
    fun `data가 없는 시그널은 null을 반환한다`() {
        val message = CallSocketMessageDto(
            type = CallSocketMessageType.OFFER,
            callId = "call-123",
            senderId = 1L,
            receiverId = 2L,
            data = null
        )

        assertNull(
            message.toCallSignal(gson)
        )
    }
}
