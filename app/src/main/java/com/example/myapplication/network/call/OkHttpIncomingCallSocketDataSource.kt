package com.example.myapplication.network.call

import android.util.Log
import com.example.myapplication.dto.IncomingCallNotificationDto
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
class OkHttpIncomingCallSocketDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : IncomingCallSocketDataSource {

    private val _incomingCallNotifications =
        MutableSharedFlow<IncomingCallNotificationDto>(extraBufferCapacity = 16)

    override val incomingCallNotifications: Flow<IncomingCallNotificationDto> =
        _incomingCallNotifications.asSharedFlow()

    private val _connectionState =
        MutableStateFlow(CallSocketConnectionState.DISCONNECTED)

    override val connectionState: StateFlow<CallSocketConnectionState> =
        _connectionState.asStateFlow()

    private class SocketSession(val id: Long) {
        val connected = CompletableDeferred<Unit>()

        @Volatile
        var socket: WebSocket? = null
    }

    private val sessionLock = Any()
    private val sessionSequence = AtomicLong(0L)

    @Volatile
    private var activeSession: SocketSession? = null

    override suspend fun connectAndSubscribe() {
        val userId = sessionManager.userId
            ?: throw IllegalStateException("로그인 사용자 정보가 없습니다.")

        val accessToken = sessionManager.accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("로그인 토큰이 없습니다.")

        val session = synchronized(sessionLock) {
            check(activeSession == null) {
                "개인 알림 소켓이 이미 연결 중이거나 연결되어 있습니다."
            }

            SocketSession(sessionSequence.incrementAndGet()).also { newSession ->
                activeSession = newSession
                _connectionState.value = CallSocketConnectionState.CONNECTING
            }
        }

        val request = Request.Builder()
            .url(SOCKET_URL)
            .header("Sec-WebSocket-Protocol", STOMP_SUBPROTOCOL)
            .build()

        val listener = createWebSocketListener(
            session = session,
            userId = userId,
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

    override suspend fun disconnect() {
        val session = synchronized(sessionLock) {
            val currentSession = activeSession
            activeSession = null
            _connectionState.value = CallSocketConnectionState.DISCONNECTED
            currentSession
        } ?: return

        session.connected.cancel()

        val socket = session.socket ?: return
        val disconnectFrame = StompFrame(command = "DISCONNECT")
        val isDisconnectQueued = socket.send(StompFrameCodec.encode(disconnectFrame))
        val isCloseQueued = socket.close(NORMAL_CLOSURE_CODE, NORMAL_CLOSURE_REASON)

        if (!isDisconnectQueued || !isCloseQueued) {
            socket.cancel()
        }

        Log.d(LOG_TAG, "Disconnected session=${session.id}")
    }

    private fun createWebSocketListener(
        session: SocketSession,
        userId: Long,
        accessToken: String
    ): WebSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (!isActiveSession(session)) {
                webSocket.cancel()
                return
            }

            val connectFrame = StompFrame(
                command = "CONNECT",
                headers = linkedMapOf(
                    "accept-version" to "1.2",
                    "host" to SOCKET_HOST,
                    "Authorization" to "Bearer $accessToken",
                    "heart-beat" to "0,0"
                )
            )

            if (!webSocket.send(StompFrameCodec.encode(connectFrame))) {
                failSession(
                    session,
                    webSocket,
                    IllegalStateException("개인 알림 STOMP 연결 요청을 전송하지 못했습니다.")
                )
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (!isActiveSession(session)) return

            StompFrameCodec.decode(text).forEach { frame ->
                when (frame.command) {
                    "CONNECTED" -> subscribe(session, webSocket, userId)
                    "MESSAGE" -> receiveNotification(session, webSocket, frame, userId)
                    "ERROR" -> failSession(
                        session,
                        webSocket,
                        IllegalStateException(
                            frame.body.ifBlank { "개인 알림 STOMP 오류가 발생했습니다." }
                        )
                    )
                }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            closeSession(
                session,
                reason.ifBlank { "개인 알림 소켓 연결이 종료되었습니다." }
            )
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            failSession(session, webSocket, t)
        }
    }

    private fun subscribe(session: SocketSession, webSocket: WebSocket, userId: Long) {
        if (!isActiveSession(session)) return

        val subscribeFrame = StompFrame(
            command = "SUBSCRIBE",
            headers = linkedMapOf(
                "id" to "incoming-call-$userId",
                "destination" to "/sub/user/$userId",
                "ack" to "auto"
            )
        )

        if (!webSocket.send(StompFrameCodec.encode(subscribeFrame))) {
            failSession(
                session,
                webSocket,
                IllegalStateException("개인 통화 알림 채널을 구독하지 못했습니다.")
            )
            return
        }

        val connected = synchronized(sessionLock) {
            if (activeSession === session) {
                _connectionState.value = CallSocketConnectionState.CONNECTED
                true
            } else {
                false
            }
        }

        if (connected) {
            Log.d(LOG_TAG, "Connected session=${session.id} userId=$userId")
            session.connected.complete(Unit)
        }
    }

    private fun receiveNotification(
        session: SocketSession,
        webSocket: WebSocket,
        frame: StompFrame,
        userId: Long
    ) {
        if (!isActiveSession(session)) return

        runCatching {
            parseIncomingCallNotification(
                body = frame.body,
                expectedReceiverId = userId,
                gson = gson
            )
        }.onSuccess { notification ->
            if (!_incomingCallNotifications.tryEmit(notification)) {
                failSession(
                    session,
                    webSocket,
                    IllegalStateException("수신 전화 알림을 처리하지 못했습니다.")
                )
            }
        }.onFailure { throwable ->
            failSession(session, webSocket, throwable)
        }
    }

    private fun isActiveSession(session: SocketSession): Boolean =
        synchronized(sessionLock) { activeSession === session }

    private fun closeSession(session: SocketSession, reason: String) {
        val wasCurrent = synchronized(sessionLock) {
            if (activeSession === session) {
                activeSession = null
                _connectionState.value = CallSocketConnectionState.DISCONNECTED
                true
            } else {
                false
            }
        }

        if (!wasCurrent) {
            Log.d(LOG_TAG, "Ignored stale onClosed session=${session.id}")
            return
        }

        session.connected.completeExceptionally(IllegalStateException(reason))
    }

    private fun failSession(
        session: SocketSession,
        socket: WebSocket?,
        throwable: Throwable
    ) {
        val wasCurrent = synchronized(sessionLock) {
            if (activeSession === session) {
                activeSession = null
                _connectionState.value = CallSocketConnectionState.FAILED
                true
            } else {
                false
            }
        }

        if (!wasCurrent) {
            Log.d(LOG_TAG, "Ignored stale onFailure session=${session.id}")
            return
        }

        Log.e(LOG_TAG, "Socket failed session=${session.id}", throwable)
        session.connected.completeExceptionally(throwable)
        socket?.cancel()
    }

    private fun cancelSessionIfActive(session: SocketSession) {
        val socket = synchronized(sessionLock) {
            if (activeSession === session) {
                activeSession = null
                _connectionState.value = CallSocketConnectionState.DISCONNECTED
                session.socket
            } else {
                null
            }
        }

        socket?.cancel()
    }

    private companion object {
        const val LOG_TAG = "IncomingCallSocket"
        const val SOCKET_URL = "ws://50.17.53.222/ws-stomp"
        const val SOCKET_HOST = "50.17.53.222"
        const val STOMP_SUBPROTOCOL = "v12.stomp"
        const val NORMAL_CLOSURE_CODE = 1000
        const val NORMAL_CLOSURE_REASON = "Incoming call socket disconnected"
    }
}
