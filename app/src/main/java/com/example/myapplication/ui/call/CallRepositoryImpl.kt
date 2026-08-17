package com.example.myapplication.ui.call

import com.example.myapplication.api.CallApiService
import com.example.myapplication.dto.CallOutRequest
import com.example.myapplication.dto.CallStatus
import com.example.myapplication.dto.ContactInsertRequest
import com.example.myapplication.dto.ContactResponse
import com.example.myapplication.dto.UpdateCallStatusRequest
import com.example.myapplication.network.SessionManager
import com.example.myapplication.ui.call.call_home.Contact
import com.example.myapplication.ui.call.call_home.DeviceContact
import com.example.myapplication.ui.call.call_receive.IncomingCall
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.call.video_call.VideoCallSession
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val callApiService: CallApiService,
    private val sessionManager: SessionManager
) : CallRepository{
    private var contacts: List<Contact> = emptyList()

    private val incomingCalls = mutableMapOf<String, IncomingCall>()
    private val videoCallSessions = mutableMapOf<String, VideoCallSession>()
    private val messageId = AtomicLong(0L)

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
        delay(MOCK_REQUEST_DELAY)

        return incomingCalls.getOrPut(callId) {
            IncomingCall(
                callId = callId,
                callerName = "엄마",
                callerProfileImageUrl = null
            )
        }
    }

    override suspend fun acceptCall(callId: String) {
        delay(MOCK_REQUEST_DELAY)

        val incomingCall = incomingCalls[callId] ?: getIncomingCall(callId)
        videoCallSessions[callId] = VideoCallSession(
            callId = callId,
            remoteUserId = null,
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

    override suspend fun sendCallMessage(
        callId: String,
        text: String
    ): CallMessage {
        check(videoCallSessions.containsKey(callId)) {
            "연결된 통화가 없습니다."
        }
        require(text.isNotBlank()) {
            "빈 메시지는 전송할 수 없습니다."
        }

        delay(MOCK_MESSAGE_DELAY)

        return CallMessage(
            id = messageId.incrementAndGet(),
            text = text,
            isMine = true
        )
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
        updateCallStatus(
            callId = callId,
            status = CallStatus.ENDED
        )

        videoCallSessions.remove(callId)
        incomingCalls.remove(callId)
    }

    private companion object {
        const val MOCK_REQUEST_DELAY = 300L
        const val MOCK_CONNECTION_DELAY = 2_000L
        const val MOCK_MESSAGE_DELAY = 200L
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