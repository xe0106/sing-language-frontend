package com.example.myapplication.ui.call

import com.example.myapplication.ui.call.call_home.Contact
import com.example.myapplication.ui.call.call_home.DeviceContact
import com.example.myapplication.ui.call.call_receive.IncomingCall
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.call.video_call.VideoCallSession


interface CallRepository {
    suspend fun getContacts():List<Contact>
    suspend fun addContact(contact: DeviceContact)
    suspend fun deleteContact(targetUserId: Long)
    suspend fun getIncomingCall(callId: Long): IncomingCall
    suspend fun acceptCall(callId: Long)
    suspend fun rejectCall(callId: Long)


    suspend fun getVideoCallSession(callId: Long): VideoCallSession
    suspend fun connectVideoCall(callId: Long)
    suspend fun sendCallMessage(callId: Long, text: String): CallMessage
    suspend fun endVideoCall(callId: Long)
}