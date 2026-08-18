package com.example.myapplication.network.call

import org.junit.Assert.assertEquals
import org.junit.Test

class StompFrameCodecTest {

    @Test
    fun `CONNECT 프레임을 인코딩한다`() {
        val frame = StompFrame(
            command = "CONNECT",
            headers = mapOf(
                "accept-version" to "1.2",
                "host" to "50.17.53.222",
                "Authorization" to "Bearer token",
                "heart-beat" to "0,0"
            )
        )

        val encoded = StompFrameCodec.encode(frame)

        val expected = buildString {
            append("CONNECT\n")
            append("accept-version:1.2\n")
            append("host:50.17.53.222\n")
            append("Authorization:Bearer token\n")
            append("heart-beat:0,0\n")
            append("\n")
            append('\u0000')
        }

        assertEquals(expected, encoded)
    }

    @Test
    fun `MESSAGE 프레임을 디코딩한다`() {
        val body = """
            {
              "type": "SUBTITLE",
              "callId": "call-123",
              "senderId": 1,
              "textContent": "안녕하세요"
            }
        """.trimIndent()

        val message = buildString {
            append("MESSAGE\n")
            append("destination:/sub/call/call-123\n")
            append("subscription:call-call-123\n")
            append("content-type:application/json\n")
            append("\n")
            append(body)
            append('\u0000')
        }

        val result = StompFrameCodec.decode(message)

        assertEquals(1, result.size)
        assertEquals("MESSAGE", result.first().command)
        assertEquals(
            "/sub/call/call-123",
            result.first().headers["destination"]
        )
        assertEquals(body, result.first().body)
    }

    @Test
    fun `heartbeat를 무시하고 여러 프레임을 디코딩한다`() {
        val firstFrame = buildString {
            append("MESSAGE\n")
            append("destination:/sub/call/call-123\n")
            append("\n")
            append("""{"type":"JOIN"}""")
            append('\u0000')
        }

        val secondFrame = buildString {
            append("MESSAGE\n")
            append("destination:/sub/call/call-123\n")
            append("\n")
            append("""{"type":"SUBTITLE"}""")
            append('\u0000')
        }

        val message = "\n$firstFrame\n$secondFrame"

        val result = StompFrameCodec.decode(message)

        assertEquals(2, result.size)
        assertEquals(
            """{"type":"JOIN"}""",
            result[0].body
        )
        assertEquals(
            """{"type":"SUBTITLE"}""",
            result[1].body
        )
    }
}
