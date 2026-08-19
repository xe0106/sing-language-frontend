package com.example.myapplication.network.call

import android.util.Log
import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType
import com.example.myapplication.dto.LandmarkFramePayload
import com.example.myapplication.network.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpCallSocketDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : CallSocketDataSource {

    private val _messages =
        MutableSharedFlow<CallSocketMessageDto>(
            extraBufferCapacity = 64
        )

    override val messages: Flow<CallSocketMessageDto> =
        _messages.asSharedFlow()

    private val _connectionState =
        MutableStateFlow(
            CallSocketConnectionState.DISCONNECTED
        )

    override val connectionState:
        StateFlow<CallSocketConnectionState> =
        _connectionState.asStateFlow()

    private class SocketSession(
        val id: Long,
        val callId: String
    ) {
        val connected = CompletableDeferred<Unit>()

        @Volatile
        var socket: WebSocket? = null
    }

    private val sessionLock = Any()
    private val sessionSequence = AtomicLong(0L)

    @Volatile
    private var activeSession: SocketSession? = null

    override suspend fun connectAndSubscribe(
        callId: String
    ) {
        val accessToken = sessionManager.accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "로그인 토큰이 없습니다."
            )

        val session = synchronized(sessionLock) {
            check(activeSession == null) {
                "이미 통화 소켓이 연결 중이거나 연결되어 있습니다."
            }

            SocketSession(
                id = sessionSequence.incrementAndGet(),
                callId = callId
            ).also { newSession ->
                activeSession = newSession
                _connectionState.value =
                    CallSocketConnectionState.CONNECTING
            }
        }

        val request = Request.Builder()
            .url(SOCKET_URL)
            .header(
                "Sec-WebSocket-Protocol",
                STOMP_SUBPROTOCOL
            )
            .build()

        val listener = createWebSocketListener(
            session = session,
            accessToken = accessToken
        )

        val socket = try {
            okHttpClient.newWebSocket(request, listener)
        } catch (throwable: Throwable) {
            failSession(session, null, throwable)
            throw throwable
        }

        val accepted = synchronized(sessionLock) {
            if (activeSession === session) {
                session.socket = socket
                true
            } else {
                false
            }
        }

        if (!accepted) {
            socket.cancel()
            session.connected.cancel()
            return
        }

        try {
            session.connected.await()
        } catch (throwable: Throwable) {
            cancelSessionIfActive(session)
            throw throwable
        }
    }

    override suspend fun send(
        message: CallSocketMessageDto
    ) {
        val session = synchronized(sessionLock) {
            check(
                _connectionState.value ==
                        CallSocketConnectionState.CONNECTED
            ) {
                "통화 소켓이 연결되어 있지 않습니다."
            }

            checkNotNull(activeSession) {
                "사용할 수 있는 통화 소켓 세션이 없습니다."
            }
        }

        require(message.callId == session.callId) {
            "현재 연결된 통화와 메시지의 callId가 다릅니다."
        }

        val socket = checkNotNull(session.socket) {
            "사용할 수 있는 WebSocket이 없습니다."
        }

        val currentUserId =  sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        require(message.senderId == currentUserId) {
            "메시지 송신자 정보가 로그인 사용자와 다릅니다."
        }

        validateOutgoingMessage(message)

        val outgoingMessage =
            if(
                message.type == CallSocketMessageType.SUBTITLE
            ) {
                message.copy(
                    subtitleId = null,
                    createdAt = null
                )
            } else {
                message
            }

        val body = gson.toJson(outgoingMessage)

        val sendFrame = StompFrame(
            command = "SEND",
            headers = linkedMapOf(
                "destination" to SIGNAL_DESTINATION,
                "content-type" to
                        "application/json;charset=UTF-8",
                "content-length" to
                        body.toByteArray(Charsets.UTF_8)
                            .size
                            .toString()
            ),
            body = body
        )

        val isQueued = socket.send(
            StompFrameCodec.encode(sendFrame)
        )

        if (!isQueued) {
            val exception = IllegalStateException(
                "소켓 메시지를 전송하지 못했습니다."
            )

            failSession(session, socket, exception)
            throw exception
        }
    }

    override suspend fun sendLandmarkFrame(
        payload: LandmarkFramePayload
    ) {
        val session = synchronized(sessionLock) {
            check(
                _connectionState.value ==
                    CallSocketConnectionState.CONNECTED
            ) {
                "통화 소켓이 연결되어 있지 않습니다."
            }

            checkNotNull(activeSession) {
                "사용할 수 있는 통화 소켓 세션이 없습니다."
            }
        }

        require(payload.callId == session.callId) {
            "현재 연결된 통화와 특징 프레임의 callId가 다릅니다."
        }

        val currentUserId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        require(payload.senderId == currentUserId) {
            "특징 프레임 송신자 정보가 로그인 사용자와 다릅니다."
        }

        validateLandmarkFrame(payload)

        val socket = checkNotNull(session.socket) {
            "사용할 수 있는 WebSocket이 없습니다."
        }
        val body = gson.toJson(payload)
        val sendFrame = StompFrame(
            command = "SEND",
            headers = linkedMapOf(
                "destination" to AI_FEATURE_DESTINATION,
                "content-type" to
                    "application/json;charset=UTF-8",
                "content-length" to
                    body.toByteArray(Charsets.UTF_8)
                        .size
                        .toString()
            ),
            body = body
        )

        if (!socket.send(StompFrameCodec.encode(sendFrame))) {
            val exception = IllegalStateException(
                "수어 특징 프레임을 전송하지 못했습니다."
            )

            failSession(session, socket, exception)
            throw exception
        }
    }

    override suspend fun disconnect(expectedCallId: String?) {
        val session = synchronized(sessionLock) {
            val currentSession = activeSession

            if (
                currentSession != null &&
                expectedCallId != null &&
                currentSession.callId != expectedCallId
            ) {
                return@synchronized null
            }

            activeSession = null
            _connectionState.value =
                CallSocketConnectionState.DISCONNECTED
            currentSession
        }

        if (session == null) {
            if (expectedCallId != null) {
                Log.d(
                    LOG_TAG,
                    "Ignored disconnect for stale callId=$expectedCallId"
                )
            }
            return
        }

        session.connected.cancel()

        val socket = session.socket ?: return

        val disconnectFrame = StompFrame(
            command = "DISCONNECT"
        )

        val isDisconnectQueued = socket.send(
            StompFrameCodec.encode(disconnectFrame)
        )

        val isCloseQueued = socket.close(
            NORMAL_CLOSURE_CODE,
            NORMAL_CLOSURE_REASON
        )

        if (!isDisconnectQueued || !isCloseQueued) {
            socket.cancel()
        }

        Log.d(
            LOG_TAG,
            "Disconnected session=${session.id} callId=${session.callId}"
        )
    }

    private fun createWebSocketListener(
        session: SocketSession,
        accessToken: String
    ): WebSocketListener = object : WebSocketListener() {

        override fun onOpen(
            webSocket: WebSocket,
            response: Response
        ) {
            if (!isActiveSession(session)) {
                webSocket.cancel()
                return
            }

            val connectFrame = StompFrame(
                command = "CONNECT",
                headers = linkedMapOf(
                    "accept-version" to "1.2",
                    "host" to SOCKET_HOST,
                    "Authorization" to
                        "Bearer $accessToken",
                    "heart-beat" to "0,0"
                )
            )

            val isQueued = webSocket.send(
                StompFrameCodec.encode(connectFrame)
            )

            if (!isQueued) {
                failSession(
                    session = session,
                    socket = webSocket,
                    throwable = IllegalStateException(
                        "STOMP 연결 요청을 전송하지 못했습니다."
                    )
                )
            }
        }

        override fun onMessage(
            webSocket: WebSocket,
            text: String
        ) {
            if (!isActiveSession(session)) return

            StompFrameCodec.decode(text)
                .forEach { frame ->
                    when (frame.command) {
                        "CONNECTED" -> subscribe(
                            session = session,
                            webSocket = webSocket,
                            callId = session.callId
                        )

                        "MESSAGE" -> receiveMessage(
                            session = session,
                            webSocket = webSocket,
                            frame = frame
                        )

                        "ERROR" -> failSession(
                            session = session,
                            socket = webSocket,
                            throwable =
                                IllegalStateException(
                                    frame.body.ifBlank {
                                        "STOMP 연결에 실패했습니다."
                                    }
                                )
                        )
                    }
                }
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String
        ) {
            closeSession(
                session = session,
                reason = reason.ifBlank {
                    "통화 소켓 연결이 종료되었습니다."
                }
            )
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            failSession(
                session = session,
                socket = webSocket,
                throwable = t
            )
        }
    }

    private fun subscribe(
        session: SocketSession,
        webSocket: WebSocket,
        callId: String
    ) {
        if (!isActiveSession(session)) return

        val subscribeFrame = StompFrame(
            command = "SUBSCRIBE",
            headers = linkedMapOf(
                "id" to "call-$callId",
                "destination" to "/sub/call/$callId",
                "ack" to "auto"
            )
        )

        val isQueued = webSocket.send(
            StompFrameCodec.encode(subscribeFrame)
        )

        if (!isQueued) {
            failSession(
                session = session,
                socket = webSocket,
                throwable = IllegalStateException(
                    "통화방 구독 요청을 전송하지 못했습니다."
                )
            )
            return
        }

        val connected = synchronized(sessionLock) {
            if (activeSession === session) {
                _connectionState.value =
                    CallSocketConnectionState.CONNECTED
                true
            } else {
                false
            }
        }

        if (connected) {
            Log.d(
                LOG_TAG,
                "Connected session=${session.id} callId=${session.callId}"
            )
            session.connected.complete(Unit)
        }
    }

    private fun receiveMessage(
        session: SocketSession,
        webSocket: WebSocket,
        frame: StompFrame
    ) {
        if (!isActiveSession(session)) return

        runCatching {
            check(frame.body.isNotBlank()) {
                "수신한 STOMP 메시지 본문이 비어 있습니다."
            }

            checkNotNull(
                gson.fromJson(
                    frame.body,
                    CallSocketMessageDto::class.java
                )
            ) {
                "수신한 소켓 메시지를 변환하지 못했습니다."
            }
        }.onSuccess { message ->
            val isEmitted = _messages.tryEmit(message)

            if (!isEmitted) {
                failSession(
                    session = session,
                    socket = webSocket,
                    throwable = IllegalStateException(
                        "수신한 소켓 메시지를 처리하지 못했습니다."
                    )
                )
            }
        }.onFailure { throwable ->
            failSession(
                session = session,
                socket = webSocket,
                throwable = throwable
            )
        }
    }

    private fun isActiveSession(
        session: SocketSession
    ): Boolean = synchronized(sessionLock) {
        activeSession === session
    }

    private fun closeSession(
        session: SocketSession,
        reason: String
    ) {
        val wasCurrent = synchronized(sessionLock) {
            if (activeSession === session) {
                activeSession = null
                _connectionState.value =
                    CallSocketConnectionState.DISCONNECTED
                true
            } else {
                false
            }
        }

        if (!wasCurrent) {
            Log.d(
                LOG_TAG,
                "Ignored stale onClosed session=${session.id} callId=${session.callId}"
            )
            return
        }

        session.connected.completeExceptionally(
            IllegalStateException(reason)
        )
    }

    private fun failSession(
        session: SocketSession,
        socket: WebSocket?,
        throwable: Throwable
    ) {
        val wasCurrent = synchronized(sessionLock) {
            if (activeSession === session) {
                activeSession = null
                _connectionState.value =
                    CallSocketConnectionState.FAILED
                true
            } else {
                false
            }
        }

        if (!wasCurrent) {
            Log.d(
                LOG_TAG,
                "Ignored stale onFailure session=${session.id} callId=${session.callId}"
            )
            return
        }

        Log.e(
            LOG_TAG,
            "Socket failed session=${session.id} callId=${session.callId}",
            throwable
        )

        session.connected.completeExceptionally(throwable)
        socket?.cancel()
    }

    private fun cancelSessionIfActive(
        session: SocketSession
    ) {
        val socket = synchronized(sessionLock) {
            if (activeSession === session) {
                activeSession = null
                _connectionState.value =
                    CallSocketConnectionState.DISCONNECTED
                session.socket
            } else {
                null
            }
        }

        socket?.cancel()
    }

    private fun validateOutgoingMessage(
        message: CallSocketMessageDto
    ) {
        when (message.type) {
            CallSocketMessageType.SUBTITLE -> {
                require(
                    !message.textContent.isNullOrBlank()
                ) {
                    "SUBTITLE 메시지에는 자막 내용이 필요합니다."
                }
            }

            CallSocketMessageType.OFFER,
            CallSocketMessageType.ANSWER,
            CallSocketMessageType.ICE_CANDIDATE -> {
                require(
                    message.data != null &&
                            !message.data.isJsonNull &&
                            message.data.isJsonObject
                ) {
                    "${message.type} 메시지에는 JSON 객체 형태의 data가 필요합니다."
                }
            }

            CallSocketMessageType.JOIN,
            CallSocketMessageType.LEAVE -> Unit

            CallSocketMessageType.CALL_STATUS_CHANGE -> {
                error(
                    "CALL_STATUS_CHANGE 메시지는 클라이언트에서 전송할 수 없습니다."
                )
            }
        }
    }

    private fun validateLandmarkFrame(
        payload: LandmarkFramePayload
    ) {
        require(
            payload.type ==
                LandmarkFramePayload.TYPE_LANDMARK_FRAME
        ) {
            "특징 프레임 type이 올바르지 않습니다."
        }
        require(payload.sessionId.isNotBlank()) {
            "특징 프레임 sessionId가 비어 있습니다."
        }
        require(payload.sequence >= 0L) {
            "특징 프레임 sequence는 0 이상이어야 합니다."
        }
        require(payload.timestampMs >= 0L) {
            "특징 프레임 timestampMs가 올바르지 않습니다."
        }
        require(payload.features.size == FEATURE_COUNT) {
            "특징 프레임은 정확히 258개 값이어야 합니다."
        }
        require(payload.features.all(Float::isFinite)) {
            "특징 프레임에는 NaN 또는 Infinity를 사용할 수 없습니다."
        }
    }

    private companion object {
        const val LOG_TAG = "CallSocket"
        const val SOCKET_URL =
            "ws://50.17.53.222/ws-stomp"
        const val SOCKET_HOST = "50.17.53.222"
        const val STOMP_SUBPROTOCOL = "v12.stomp"
        const val SIGNAL_DESTINATION = "/pub/call/signal"
        const val AI_FEATURE_DESTINATION = "/pub/ai/features"
        const val FEATURE_COUNT = 258

        const val NORMAL_CLOSURE_CODE = 1000
        const val NORMAL_CLOSURE_REASON = "Call socket disconnected"
    }
}
