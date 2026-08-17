package com.example.myapplication.ui.call.video_call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType
import com.example.myapplication.dto.IceCandidateDataDto
import com.example.myapplication.dto.SdpDataDto
import com.google.gson.Gson

internal fun CallSocketMessageDto.toCallSignal(
    gson: Gson
): CallSignal? =
    runCatching {
        val signalData = requireNotNull(data) {
            "시그널 메시지에 data가 없습니다."
        }

        val signalSenderId = requireNotNull(senderId) {
            "시그널링 메시지에 senderId가 없습니다."
        }

        when (type) {
            CallSocketMessageType.OFFER -> {
                val sdpData = gson.fromJson(
                    signalData,
                    SdpDataDto::class.java
                )

                require(
                    sdpData.type == "offer" &&
                            sdpData.sdp.isNotBlank()
                )

                CallSignal.Offer(
                    callId = callId,
                    senderId = signalSenderId,
                    receiverId = receiverId,
                    sdp = sdpData.sdp
                )
            }

            CallSocketMessageType.ANSWER -> {
                val sdpData = gson.fromJson(
                    signalData,
                    SdpDataDto::class.java
                )

                require(
                    sdpData.type == "answer" &&
                            sdpData.sdp.isNotBlank()
                )

                CallSignal.Answer(
                    callId = callId,
                    senderId = signalSenderId,
                    receiverId = receiverId,
                    sdp = sdpData.sdp
                )
            }

            CallSocketMessageType.ICE_CANDIDATE -> {
                val candidateData = gson.fromJson(
                    signalData,
                    IceCandidateDataDto::class.java
                )

                require(
                    candidateData.candidate.isNotBlank()
                )

                CallSignal.IceCandidate(
                    callId = callId,
                    senderId = signalSenderId,
                    receiverId = receiverId,
                    candidate = candidateData.candidate,
                    sdpMid = candidateData.sdpMid,
                    sdpMLineIndex =
                        candidateData.sdpMLineIndex
                )
            }

            else -> error(
                "WebRTC 시그널 메시지가 아닙니다."
            )
        }
    }.getOrNull()