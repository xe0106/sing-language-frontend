package com.example.myapplication.ui.call

import com.example.myapplication.api.CallApiService
import com.example.myapplication.dto.ContactInsertRequest
import com.example.myapplication.dto.ContactResponse
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
    private val callApiService: CallApiService
) : CallRepository{
    private var contacts: List<Contact> = emptyList()

    private val incomingCalls = mutableMapOf<Long, IncomingCall>()
    private val videoCallSessions = mutableMapOf<Long, VideoCallSession>()
    private val messageId = AtomicLong(0L)

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

    override suspend fun getIncomingCall(callId: Long): IncomingCall {
        delay(MOCK_REQUEST_DELAY)

        return incomingCalls.getOrPut(callId) {
            IncomingCall(
                callId = callId,
                callerName = "엄마",
                callerProfileImageUrl = null
            )
        }
    }

    override suspend fun acceptCall(callId: Long) {
        delay(MOCK_REQUEST_DELAY)

        val incomingCall = incomingCalls[callId] ?: getIncomingCall(callId)
        videoCallSessions[callId] = VideoCallSession(
            callId = callId,
            remoteName = incomingCall.callerName,
            isOutgoing = false
        )
    }

    override suspend fun rejectCall(callId: Long) {
        delay(MOCK_REQUEST_DELAY)
        incomingCalls.remove(callId)
    }

    override suspend fun getVideoCallSession(callId: Long): VideoCallSession {
        delay(MOCK_REQUEST_DELAY)

        return videoCallSessions.getOrPut(callId) {
            val contact = contacts.firstOrNull { it.contactId == callId }

            VideoCallSession(
                callId = callId,
                remoteName = contact?.name ?: "상대방",
                isOutgoing = true
            )
        }
    }

    override suspend fun connectVideoCall(callId: Long) {
        check(videoCallSessions.containsKey(callId)) {
            "존재하지 않는 통화입니다."
        }
        delay(MOCK_CONNECTION_DELAY)
    }

    override suspend fun sendCallMessage(
        callId: Long,
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

    override suspend fun endVideoCall(callId: Long) {
        delay(MOCK_REQUEST_DELAY)
        videoCallSessions.remove(callId)
        incomingCalls.remove(callId)
    }

    private companion object {
        const val MOCK_REQUEST_DELAY = 300L
        const val MOCK_CONNECTION_DELAY = 2_000L
        const val MOCK_MESSAGE_DELAY = 200L
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