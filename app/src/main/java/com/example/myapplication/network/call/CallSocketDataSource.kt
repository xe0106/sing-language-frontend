package com.example.myapplication.network.call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.LandmarkFramePayload
import com.example.myapplication.dto.SignSessionEndPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CallSocketDataSource {
    val messages: Flow<CallSocketMessageDto>

    val connectionState: StateFlow<CallSocketConnectionState>

    suspend fun connectAndSubscribe(callId: String)

    suspend fun send(message: CallSocketMessageDto)

    suspend fun sendLandmarkFrame(
        payload: LandmarkFramePayload
    )

    suspend fun sendSignSessionEnd(
        payload: SignSessionEndPayload
    )

    suspend fun disconnect(expectedCallId: String? = null)

}
