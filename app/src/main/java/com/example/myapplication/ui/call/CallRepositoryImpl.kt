package com.example.myapplication.ui.call

import com.example.myapplication.api.CallApiService
import com.example.myapplication.dto.CallOutRequest
import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType
import com.example.myapplication.dto.CallStatus
import com.example.myapplication.dto.ContactInsertRequest
import com.example.myapplication.dto.ContactResponse
import com.example.myapplication.dto.IceCandidateDataDto
import com.example.myapplication.dto.SdpDataDto
import com.example.myapplication.dto.UpdateCallStatusRequest
import com.example.myapplication.network.SessionManager
import com.example.myapplication.network.call.CallSocketConnectionState
import com.example.myapplication.network.call.CallSocketDataSource
import com.example.myapplication.network.call.IncomingCallSocketDataSource
import com.example.myapplication.ui.call.call_home.Contact
import com.example.myapplication.ui.call.call_home.DeviceContact
import com.example.myapplication.ui.call.call_receive.IncomingCall
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.call.video_call.CallSignal
import com.example.myapplication.ui.call.video_call.CallStatusChange
import com.example.myapplication.ui.call.video_call.VideoCallSession
import com.example.myapplication.ui.call.video_call.toCallSignal
import com.example.myapplication.ui.call.video_call.toCallStatusChange
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val callApiService: CallApiService,
    private val sessionManager: SessionManager,
    private val callSocketDataSource: CallSocketDataSource,
    private val incomingCallSocketDataSource: IncomingCallSocketDataSource,
    private val gson: Gson
) : CallRepository{
    private var contacts: List<Contact> = emptyList()

    private val incomingCalls = mutableMapOf<String, IncomingCall>()
    private val videoCallSessions = mutableMapOf<String, VideoCallSession>()

    override suspend fun startCall(receiverId: Long): VideoCallSession {
        val callerId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        val response = callApiService.callOut(
            request = CallOutRequest(
                callerId = callerId,
                receiverId = receiverId
            )
        )

        val body = response.body()
        val call = body?.data

        if(
            !response.isSuccessful ||
            body?.isSuccess != true ||
            call == null
        ) {
            throw IllegalStateException(
                body?.message ?: "전화를 발신하지 못했습니다."
            )
        }

        val contact = contacts.firstOrNull {
            it.targetUserId == receiverId
        }

        val session = VideoCallSession(
            callId = call.callId,
            remoteUserId = call.receiverId,
            remoteName = contact?.name ?: "상대방",
            isOutgoing = true
        )

        videoCallSessions[call.callId] = session

        return session
    }

    override suspend fun getContacts(): List<Contact> {
        /*delay(MOCK_REQUEST_DELAY)
        return contacts.toList()*/

        val response = callApiService.viewContactList()

        val body = response.body()

        if(
            !response.isSuccessful ||
            body?.isSuccess != true
        ) {
            throw IllegalStateException(
                body?.message ?: "연락처 목록 조회에 실패했습니다."
            )
        }

        return body.data.orEmpty()
            .map(ContactResponse::toContact)
            .also {contacts = it}
    }

    override suspend fun addContact(contact: DeviceContact) {
        val response = callApiService.insertContact(
            request = ContactInsertRequest(
                phoneNumber = normalizePhoneNumber(contact.phoneNumber),
                contactName = contact.name,
                profileImageUrl = null
            )
        )

        val body = response.body()

        if(
            !response.isSuccessful ||
            body?.isSuccess != true
        ) {
            throw IllegalStateException(
                body?.message ?: "연락처 추가에 실패했습니다."
            )
        }
    }

    override suspend fun deleteContact(targetUserId: Long) {
        val response = callApiService.deleteContact(
            targetUserId = targetUserId
        )

        val body = response.body()

        if(
            !response.isSuccessful ||
            body?.isSuccess != true
        ) {
            throw IllegalStateException(
                body?.message ?: "연락처 삭제에 실패했습니다."
            )
        }
    }

    override suspend fun getIncomingCall(callId: String): IncomingCall {
        incomingCalls[callId]?.let { incomingCall ->
            return incomingCall
        }

        delay(MOCK_REQUEST_DELAY)

        return incomingCalls.getOrPut(callId) {
            IncomingCall(
                callId = callId,
                callerName = "상대방",
                callerProfileImageUrl = null
            )
        }
    }

    override suspend fun acceptCall(callId: String) {
        val incomingCall = incomingCalls[callId] ?: getIncomingCall(callId)

        updateCallStatus(
            callId = callId,
            status = CallStatus.CONNECTED
        )

        videoCallSessions[callId] = VideoCallSession(
            callId = callId,
            remoteUserId = incomingCall.callerId,
            remoteName = incomingCall.callerName,
            isOutgoing = false
        )
    }

    override suspend fun rejectCall(callId: String) {
        updateCallStatus(
            callId = callId,
            status = CallStatus.REJECTED
        )

        incomingCalls.remove(callId)
    }

    override suspend fun getVideoCallSession(callId: String): VideoCallSession {
        delay(MOCK_REQUEST_DELAY)

        return videoCallSessions.getOrPut(callId) {
            VideoCallSession(
                callId = callId,
                remoteUserId = null,
                remoteName = "상대방",
                isOutgoing = true
            )
        }
    }

    override suspend fun connectVideoCall(callId: String) {
        check(videoCallSessions.containsKey(callId)) {
            "존재하지 않는 통화입니다."
        }
        delay(MOCK_CONNECTION_DELAY)
    }


    override suspend fun getSubtitles(callId: String): List<CallMessage> {
        val response = callApiService.viewSubtitleList(callId)
        val body = response.body()
        val subtitles = body?.data

        if(
            !response.isSuccessful ||
            body?.isSuccess !=true ||
            subtitles == null
        ) {
            throw IllegalStateException(
                body?.message ?: "자막 목록을 불러오지 못했습니다."
            )
        }

        val currentUserId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        return subtitles
            .sortedBy { it.createdAt }
            .map { subtitle ->
                CallMessage(
                    id = subtitle.subtitleId,
                    text = subtitle.textContent,
                    isMine = subtitle.senderId == currentUserId,
                    createdAt = subtitle.createdAt
                )
            }
    }

    override suspend fun endVideoCall(callId: String) {
        try {
            val senderId = sessionManager.userId
                ?: throw IllegalStateException(
                    "로그인 사용자 정보가 없습니다."
                )

            val receiverId = videoCallSessions[callId]?.remoteUserId

            if(
                callSocketDataSource.connectionState.value == CallSocketConnectionState.CONNECTED
            ) {
                runCatching {
                    callSocketDataSource.send(
                        CallSocketMessageDto(
                            type = CallSocketMessageType.LEAVE,
                            callId = callId,
                            senderId = senderId,
                            receiverId = receiverId
                        )
                    )
                }
            }

            updateCallStatus(
                callId = callId,
                status = CallStatus.ENDED
            )

            videoCallSessions.remove(callId)
            incomingCalls.remove(callId)
        } finally {
            callSocketDataSource.disconnect()
        }
    }

    private companion object {
        const val MOCK_REQUEST_DELAY = 300L
        const val MOCK_CONNECTION_DELAY = 2_000L
    }

    override suspend fun updateCallStatus(callId: String, status: CallStatus) {
        val response = callApiService.updateCallStatus(
            callId = callId,
            request = UpdateCallStatusRequest(
                status = status
            )
        )

        val body = response.body()
        val updatedCall = body?.data

        if(
            !response.isSuccessful ||
            body?.isSuccess != true ||
            updatedCall == null
        ) {
            throw IllegalStateException(
                body?.message ?: "통화 상태를 변경하지 못했습니다."
            )
        }
    }

    override val callSocketMessages = callSocketDataSource.messages

    override val callSocketConnectionState = callSocketDataSource.connectionState

    override suspend fun connectCallSocket(
        callId: String,
        receiverId: Long?
    ) {
        val senderId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        callSocketDataSource.connectAndSubscribe(callId)

        try {
            callSocketDataSource.send(
                CallSocketMessageDto(
                    type = CallSocketMessageType.JOIN,
                    callId = callId,
                    senderId = senderId,
                    receiverId = receiverId
                )
            )
        } catch (throwable: Throwable) {
            callSocketDataSource.disconnect()
            throw throwable
        }
    }

    override suspend fun disconnectCallSocket() {
        callSocketDataSource.disconnect()
    }

    override val incomingCallNotifications =
        incomingCallSocketDataSource
            .incomingCallNotifications

    override val incomingCallEvents:
            Flow<IncomingCall> =
        incomingCallNotifications.map { notification ->
            IncomingCall(
                callId = notification.callId,
                callerName =
                    "사용자 ${notification.callerId}",
                callerProfileImageUrl = null,
                callerId = notification.callerId
            ).also { incomingCall ->
                incomingCalls[incomingCall.callId] =
                    incomingCall
            }
        }

    override val incomingCallSocketConnectionState =
        incomingCallSocketDataSource
            .connectionState

    override suspend fun connectIncomingCallSocket() {
        incomingCallSocketDataSource
            .connectAndSubscribe()
    }

    override suspend fun disconnectIncomingCallSocket() {
        incomingCallSocketDataSource.disconnect()
    }

    override suspend fun sendSubtitle(
        callId: String,
        receiverId: Long?,
        text: String
    ) {
        val senderId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        val textContent = text.trim()

        require(textContent.isNotEmpty()) {
            "빈 자막은 전송할 수 없습니다."
        }

        callSocketDataSource.send(
            CallSocketMessageDto(
                type = CallSocketMessageType.SUBTITLE,
                callId = callId,
                senderId = senderId,
                receiverId = receiverId,
                textContent = textContent
            )
        )
    }

    override val subtitleMessages: Flow<CallMessage> =
        callSocketDataSource.messages.mapNotNull { message ->
            if (message.type != CallSocketMessageType.SUBTITLE) {
                return@mapNotNull null
            }

            val subtitleId = message.subtitleId
                ?: return@mapNotNull null

            val textContent = message.textContent
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            val currentUserId = sessionManager.userId
                ?: return@mapNotNull null

            CallMessage(
                id = subtitleId,
                text = textContent,
                isMine = message.senderId == currentUserId,
                createdAt = message.createdAt
            )
        }

    override val remoteLeaveCallIds: Flow<String> =
        callSocketDataSource.messages.mapNotNull { message ->
            val currentUserId = sessionManager.userId
                ?: return@mapNotNull null

            if(
                message.type == CallSocketMessageType.LEAVE &&
                message.senderId !=currentUserId
            ) {
                message.callId
            } else {
                null
            }
        }

    override val callStatusChanges: Flow<CallStatusChange> =
        callSocketDataSource.messages.mapNotNull { message ->
            message.toCallStatusChange()
        }

    override val signalingMessages: Flow<CallSocketMessageDto> =
        callSocketDataSource.messages.filter { message ->
            when (message.type) {
                CallSocketMessageType.OFFER,
                CallSocketMessageType.ANSWER,
                CallSocketMessageType.ICE_CANDIDATE -> true

                else -> false
            }
        }

    override val remoteCallSignals: Flow<CallSignal> =
        signalingMessages.mapNotNull { message ->
            val currentUserId = sessionManager.userId
                ?: return@mapNotNull null

            if (message.senderId == currentUserId) {
                return@mapNotNull null
            }

            message.toCallSignal(gson)
        }

    override suspend fun handleRemoteCallEnded(callId: String) {
        try {
            callSocketDataSource.disconnect()
        } finally {
            videoCallSessions.remove(callId)
            incomingCalls.remove(callId)
        }
    }

    private suspend fun sendSignalingMessage(
        type: CallSocketMessageType,
        callId: String,
        receiverId: Long?,
        data: JsonObject
    ) {
        val senderId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        callSocketDataSource.send(
            CallSocketMessageDto(
                type = type,
                callId = callId,
                senderId = senderId,
                receiverId = receiverId,
                data = data
            )
        )
    }

    override suspend fun sendOffer(callId: String, receiverId: Long?, sdp: String) {
        require(sdp.isNotBlank()) {
            "OFFER SDP가 비어 있습니다."
        }

        val data = gson.toJsonTree(
            SdpDataDto(
                type = "offer",
                sdp = sdp
            )
        ).asJsonObject

        sendSignalingMessage(
            type = CallSocketMessageType.OFFER,
            callId = callId,
            receiverId = receiverId,
            data = data
        )
    }

    override suspend fun sendAnswer(callId: String, receiverId: Long?, sdp: String) {
        require(sdp.isNotBlank()) {
            "ANSWER SDP가 비어 있습니다."
        }

        val data = gson.toJsonTree(
            SdpDataDto(
                type = "answer",
                sdp = sdp
            )
        ).asJsonObject

        sendSignalingMessage(
            type = CallSocketMessageType.ANSWER,
            callId = callId,
            receiverId = receiverId,
            data = data
        )
    }

    override suspend fun sendIceCandidate(
        callId: String,
        receiverId: Long?,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    ) {
        require(candidate.isNotBlank()) {
            "ICE Candidate가 비어 있습니다."
        }

        val data = gson.toJsonTree(
            IceCandidateDataDto(
                candidate = candidate,
                sdpMid = sdpMid,
                sdpMLineIndex = sdpMLineIndex
            )
        ).asJsonObject

        sendSignalingMessage(
            type = CallSocketMessageType.ICE_CANDIDATE,
            callId = callId,
            receiverId = receiverId,
            data = data
        )
    }

}

private fun ContactResponse.toContact(): Contact {
    return Contact(
        contactId = contactId,
        targetUserId = targetUserId,
        name = contactName,
        profileImageUrl = profileImageUrl
    )
}

private fun normalizePhoneNumber(
    phoneNumber: String
): String {
    val digits = phoneNumber.filter(Char::isDigit)

    return if (digits.startsWith("82")) {
        "0${digits.removePrefix("82")}"
    } else {
        digits
    }
}