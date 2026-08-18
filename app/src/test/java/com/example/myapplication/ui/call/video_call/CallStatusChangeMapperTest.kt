package com.example.myapplication.ui.call.video_call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType
import com.example.myapplication.dto.CallStatus
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallStatusChangeMapperTest {

    private val gson = Gson()

    @Test
    fun `CONNECTED payload maps to status change`() {
        val message = parseMessage(
            """
            {
              "type": "CALL_STATUS_CHANGE",
              "callId": "call-123",
              "callerId": 1,
              "receiverId": 2,
              "status": "CONNECTED",
              "startedAt": "2026-08-17T19:50:00",
              "endedAt": null
            }
            """.trimIndent()
        )

        assertEquals(
            CallStatusChange(
                callId = "call-123",
                callerId = 1L,
                receiverId = 2L,
                status = CallStatus.CONNECTED,
                startedAt = "2026-08-17T19:50:00",
                endedAt = null
            ),
            message.toCallStatusChange()
        )
    }

    @Test
    fun `REJECTED payload maps with ended time`() {
        val result = parseMessage(
            """
            {
              "type": "CALL_STATUS_CHANGE",
              "callId": "call-123",
              "callerId": 1,
              "receiverId": 2,
              "status": "REJECTED",
              "startedAt": "2026-08-17T19:50:00",
              "endedAt": "2026-08-17T19:51:20"
            }
            """.trimIndent()
        ).toCallStatusChange()

        assertEquals(CallStatus.REJECTED, result?.status)
        assertEquals("2026-08-17T19:51:20", result?.endedAt)
    }

    @Test
    fun `ENDED payload maps to ended status`() {
        val result = parseMessage(
            """
            {
              "type": "CALL_STATUS_CHANGE",
              "callId": "call-123",
              "callerId": 1,
              "receiverId": 2,
              "status": "ENDED",
              "startedAt": "2026-08-17T19:50:00",
              "endedAt": "2026-08-17T19:55:00"
            }
            """.trimIndent()
        ).toCallStatusChange()

        assertEquals(CallStatus.ENDED, result?.status)
        assertEquals("2026-08-17T19:55:00", result?.endedAt)
    }

    @Test
    fun `non status message returns null`() {
        val message = CallSocketMessageDto(
            type = CallSocketMessageType.JOIN,
            callId = "call-123",
            senderId = 1L,
            receiverId = 2L
        )

        assertNull(message.toCallStatusChange())
    }

    @Test
    fun `status message without required fields returns null`() {
        val message = CallSocketMessageDto(
            type = CallSocketMessageType.CALL_STATUS_CHANGE,
            callId = "call-123",
            callerId = null,
            receiverId = 2L,
            status = CallStatus.CONNECTED
        )

        assertNull(message.toCallStatusChange())
    }

    private fun parseMessage(json: String): CallSocketMessageDto =
        gson.fromJson(
            json,
            CallSocketMessageDto::class.java
        )
}
