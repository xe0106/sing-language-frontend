package com.example.myapplication.ui.call.video_call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType

internal fun CallSocketMessageDto.toCallStatusChange():
        CallStatusChange? {

    if (
        type !=
        CallSocketMessageType.CALL_STATUS_CHANGE
    ) {
        return null
    }

    if (callId.isBlank()) {
        return null
    }

    val statusCallerId =
        callerId ?: return null

    val statusReceiverId =
        receiverId ?: return null

    val callStatus =
        status ?: return null

    return CallStatusChange(
        callId = callId,
        callerId = statusCallerId,
        receiverId = statusReceiverId,
        status = callStatus,
        startedAt = startedAt,
        endedAt = endedAt
    )
}