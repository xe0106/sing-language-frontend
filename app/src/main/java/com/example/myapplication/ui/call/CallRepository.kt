package com.example.myapplication.ui.call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallStatus
import com.example.myapplication.dto.IncomingCallNotificationDto
import com.example.myapplication.network.call.CallSocketConnectionState
import com.example.myapplication.ui.call.call_home.Contact
import com.example.myapplication.ui.call.call_home.DeviceContact
import com.example.myapplication.ui.call.call_receive.IncomingCall
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.call.video_call.CallSignal
import com.example.myapplication.ui.call.video_call.CallStatusChange
import com.example.myapplication.ui.call.video_call.VideoCallSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


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
    suspend fun getSubtitles(callId: String): List<CallMessage>
    suspend fun endVideoCall(callId: String)
    suspend fun updateCallStatus(
        callId: String,
        status: CallStatus
    )

    // 통화방 소켓
    val callSocketMessages: Flow<CallSocketMessageDto>
    val callSocketConnectionState: StateFlow<CallSocketConnectionState>

    suspend fun connectCallSocket(
        callId: String,
        receiverId: Long?
    )

    suspend fun disconnectCallSocket()

    // 개인 수신 전화 알림 소켓
    val incomingCallNotifications:
            Flow<IncomingCallNotificationDto>

    val incomingCallEvents:
            Flow<IncomingCall>

    val incomingCallSocketConnectionState:
            StateFlow<CallSocketConnectionState>

    suspend fun connectIncomingCallSocket()

    suspend fun disconnectIncomingCallSocket()

    suspend fun sendSubtitle(
        callId: String,
        receiverId: Long?,
        text: String
    )

    val subtitleMessages: Flow<CallMessage>


    val remoteJoinCallIds: Flow<String>
    val remoteLeaveCallIds: Flow<String>

    val callStatusChanges: Flow<CallStatusChange>

    val signalingMessages: Flow<CallSocketMessageDto>

    val remoteCallSignals: Flow<CallSignal>
    suspend fun handleRemoteCallEnded(callId: String)

    suspend fun sendOffer(
        callId: String,
        receiverId: Long?,
        sdp: String
    )

    suspend fun sendAnswer(
        callId: String,
        receiverId: Long?,
        sdp: String
    )

    suspend fun sendIceCandidate(
        callId: String,
        receiverId: Long?,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    )

}