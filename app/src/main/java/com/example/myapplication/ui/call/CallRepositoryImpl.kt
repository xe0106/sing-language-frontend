package com.example.myapplication.ui.call

import com.example.myapplication.ui.call.call_home.Contact
import com.example.myapplication.ui.call.call_receive.IncomingCall
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.call.video_call.VideoCallSession
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor() : CallRepository{
    private val contacts = mutableListOf<Contact>(
        Contact(id = 1L, name = "엄마", phoneNumber = "010-0000-0001"),
        Contact(id = 2L, name = "동생", phoneNumber = "010-0000-0002")
    )

    private val incomingCalls = mutableMapOf<Long, IncomingCall>()
    private val videoCallSessions = mutableMapOf<Long, VideoCallSession>()
    private val messageId = AtomicLong(0L)

    override suspend fun getContacts(): List<Contact> {
        delay(MOCK_REQUEST_DELAY)
        return contacts.toList()
    }

    override suspend fun addContact(contact: Contact) {
        delay(MOCK_REQUEST_DELAY)

        val newContact = contact.copy(
            id=(contacts.maxOfOrNull {it.id}?:0L)+1L
        )
        contacts.add(newContact)
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
            val contact = contacts.firstOrNull { it.id == callId }

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
