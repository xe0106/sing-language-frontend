package com.example.myapplication.network.call

import com.example.myapplication.dto.IncomingCallNotificationDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IncomingCallSocketDataSource {

    // /sub/user/{userId}에서 수신한 INCOMING_CALL 메시지를 앱에 전달
    val incomingCallNotifications:
            Flow<IncomingCallNotificationDto>

    // 개인 알림 소켓의 상태를 전달
    val connectionState:
            StateFlow<CallSocketConnectionState>

    suspend fun connectAndSubscribe()

    suspend fun disconnect()
}