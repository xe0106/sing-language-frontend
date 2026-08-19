package com.example.myapplication.dto

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class LandmarkFramePayloadTest {

    @Test
    fun `AI 전송 명세와 동일한 JSON을 생성한다`() {
        val payload = LandmarkFramePayload(
            sessionId =
                "b2683ee7-1435-4726-9e64-d07eb0c56613",
            callId =
                "8f3a5b21-4d1e-4f32-8a90-123456789abc",
            senderId = 16L,
            sequence = 2L,
            timestampMs = 1_723_100_000_123L,
            features = FloatArray(258) { index ->
                index / 1_000f
            }
        )

        val json = JsonParser.parseString(
            Gson().toJson(payload)
        ).asJsonObject

        assertEquals(
            "landmark_frame",
            json["type"].asString
        )
        assertEquals(
            payload.sessionId,
            json["sessionId"].asString
        )
        assertEquals(payload.callId, json["callId"].asString)
        assertEquals(payload.senderId, json["senderId"].asLong)
        assertEquals(payload.sequence, json["sequence"].asLong)
        assertEquals(
            payload.timestampMs,
            json["timestampMs"].asLong
        )
        assertEquals(258, json["features"].asJsonArray.size())
    }
}
