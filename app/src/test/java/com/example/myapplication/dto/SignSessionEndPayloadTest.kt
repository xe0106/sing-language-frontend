package com.example.myapplication.dto

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SignSessionEndPayloadTest {

    @Test
    fun `발화 종료 명세와 동일한 JSON을 생성한다`() {
        val payload = SignSessionEndPayload(
            callId =
                "5165d17f-cdf4-4a1e-9ccf-ce306689c832",
            sessionId =
                "303dc41a-9798-44b6-8de0-1a7b45a078ac",
            senderId = 15L,
            timestampMs = 1_724_000_005_000L
        )

        val json = JsonParser.parseString(
            Gson().toJson(payload)
        ).asJsonObject

        assertEquals("session_end", json["type"].asString)
        assertEquals(payload.callId, json["callId"].asString)
        assertEquals(
            payload.sessionId,
            json["sessionId"].asString
        )
        assertEquals(payload.senderId, json["senderId"].asLong)
        assertEquals(
            payload.timestampMs,
            json["timestampMs"].asLong
        )
        assertFalse(json.has("sequence"))
        assertFalse(json.has("features"))
    }
}
