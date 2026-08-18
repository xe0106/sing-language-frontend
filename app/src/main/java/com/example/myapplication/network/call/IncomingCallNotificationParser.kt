package com.example.myapplication.network.call

import com.example.myapplication.dto.IncomingCallNotificationDto
import com.example.myapplication.dto.IncomingCallNotificationType
import com.google.gson.Gson

internal fun parseIncomingCallNotification(
    body: String,
    expectedReceiverId: Long,
    gson: Gson
): IncomingCallNotificationDto {
    check(body.isNotBlank()) {
        "수신한 개인 알림 본문이 비어 있습니다."
    }

    val notification = checkNotNull(
        gson.fromJson(
            body,
            IncomingCallNotificationDto::class.java
        )
    ) {
        "개인 통화 알림을 변환하지 못했습니다."
    }

    require(
        notification.type ==
                IncomingCallNotificationType.INCOMING_CALL
    ) {
        "수신 전화 알림이 아닙니다."
    }

    require(notification.callerId > 0L) {
        "발신자 ID가 유효하지 않습니다."
    }

    require(
        notification.receiverId ==
                expectedReceiverId
    ) {
        "현재 로그인 사용자의 수신 전화가 아닙니다."
    }

    require(notification.callId.isNotBlank()) {
        "수신 전화의 callId가 비어 있습니다."
    }

    require(notification.status == "RINGING") {
        "발신 중인 통화가 아닙니다."
    }

    return notification
}