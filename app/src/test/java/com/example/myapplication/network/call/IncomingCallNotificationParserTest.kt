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
                callerName = "홍길동",
                callerNickname = "수어왕",
                callerProfileImageUrl = "https://example.com/profile.jpg",
                receiverId = 14L,
                status = "RINGING",
                startedAt = "2026-08-18T14:00:00",
                endedAt = null
            ),
            result
        )
    }

    @Test
    fun `payload without optional caller information is parsed`() {
        val result = parseIncomingCallNotification(
            body = """
                {
                  "type": "INCOMING_CALL",
                  "callId": "call-legacy",
                  "callerId": 1,
                  "receiverId": 14,
                  "status": "RINGING"
                }
            """.trimIndent(),
            expectedReceiverId = 14L,
            gson = gson
        )

        assertEquals(null, result.callerName)
        assertEquals(null, result.callerNickname)
        assertEquals(null, result.callerProfileImageUrl)
        assertEquals(null, result.startedAt)
        assertEquals(null, result.endedAt)
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
          "callerName": "홍길동",
          "callerNickname": "수어왕",
          "callerProfileImageUrl": "https://example.com/profile.jpg",
          "receiverId": $receiverId,
          "status": "$status",
          "startedAt": "2026-08-18T14:00:00",
          "endedAt": null
        }
        """.trimIndent()
}
