package com.example.myapplication.ui.call

import com.example.myapplication.dto.CallStatus
import com.example.myapplication.ui.call.call_home.Contact
import com.example.myapplication.ui.call.call_home.DeviceContact
import com.example.myapplication.ui.call.call_receive.IncomingCall
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.call.video_call.VideoCallSession


interface CallRepository {
    suspend fun startCall(receiverId: Long): VideoCallSession
    suspend fun getContacts():List<Contact>
    suspend fun addContact(contact: DeviceContact)
    suspend fun deleteContact(targetUserId: Long)
    suspend fun getIncomingCall(callId: String): IncomingCall
    suspend fun acceptCall(callId: String)
    suspend fun rejectCall(callId: String)
    suspend fun getVideoCallSession(callId: String): VideoCallSession
    suspend fun connectVideoCall(callId: String)
    suspend fun sendCallMessage(callId: String, text: String): CallMessage
    suspend fun getSubtitles(callId: String): List<CallMessage>
    suspend fun endVideoCall(callId: String)
    suspend fun updateCallStatus(
        callId: String,
        status: CallStatus
    )
}