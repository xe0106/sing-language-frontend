package com.example.myapplication.network.call

import com.example.myapplication.dto.IncomingCallNotificationDto
import com.example.myapplication.dto.IncomingCallNotificationType
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingCallNotificationParserTest {

    private val gson = Gson()

    @Test
    fun `valid incoming call payload is parsed`() {
        val result = parseIncomingCallNotification(
            body = validPayload(),
            expectedReceiverId = 14L,
            gson = gson
        )

        assertEquals(
            IncomingCallNotificationDto(
                type = IncomingCallNotificationType.INCOMING_CALL,
                callId = "call-1234-abcd",
                callerId = 1L,
                receiverId = 14L,
                status = "RINGING"
            ),
            result
        )
    }

    @Test
    fun `payload for a different receiver fails`() {
        val result = runCatching {
            parseIncomingCallNotification(
                body = validPayload(receiverId = 15L),
                expectedReceiverId = 14L,
                gson = gson
            )
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `payload with blank call id fails`() {
        val result = runCatching {
            parseIncomingCallNotification(
                body = validPayload(callId = ""),
                expectedReceiverId = 14L,
                gson = gson
            )
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `payload that is not ringing fails`() {
        val result = runCatching {
            parseIncomingCallNotification(
                body = validPayload(status = "CONNECTED"),
                expectedReceiverId = 14L,
                gson = gson
            )
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `payload with invalid caller id fails`() {
        val result = runCatching {
            parseIncomingCallNotification(
                body = validPayload(callerId = 0L),
                expectedReceiverId = 14L,
                gson = gson
            )
        }

        assertTrue(result.isFailure)
    }

    private fun validPayload(
        callId: String = "call-1234-abcd",
        callerId: Long = 1L,
        receiverId: Long = 14L,
        status: String = "RINGING"
    ): String =
        """
        {
          "type": "INCOMING_CALL",
          "callId": "$callId",
          "callerId": $callerId,
          "receiverId": $receiverId,
          "status": "$status",
          "createdAt": "2026-08-17T18:00:00+09:00"
        }
        """.trimIndent()
}
