package com.example.myapplication.network.call

import com.example.myapplication.dto.IncomingCallNotificationDto
import com.example.myapplication.network.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OkHttpIncomingCallSocketDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : IncomingCallSocketDataSource {

    private val _incomingCallNotifications =
        MutableSharedFlow<IncomingCallNotificationDto>(
            extraBufferCapacity = 16
        )

    override val incomingCallNotifications:
            Flow<IncomingCallNotificationDto> =
        _incomingCallNotifications.asSharedFlow()

    private val _connectionState =
        MutableStateFlow(
            CallSocketConnectionState.DISCONNECTED
        )

    override val connectionState:
            StateFlow<CallSocketConnectionState> =
        _connectionState.asStateFlow()

    private var webSocket: WebSocket? = null

    override suspend fun connectAndSubscribe() {
        check(
            _connectionState.value !=
                    CallSocketConnectionState.CONNECTING &&
                    _connectionState.value !=
                    CallSocketConnectionState.CONNECTED
        ) {
            "개인 알림 소켓이 이미 연결 중이거나 연결되어 있습니다."
        }

        val userId = sessionManager.userId
            ?: throw IllegalStateException(
                "로그인 사용자 정보가 없습니다."
            )

        val accessToken = sessionManager.accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "로그인 토큰이 없습니다."
            )

        _connectionState.value =
            CallSocketConnectionState.CONNECTING

        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url(SOCKET_URL)
                .header(
                    "Sec-WebSocket-Protocol",
                    STOMP_SUBPROTOCOL
                )
                .build()

            val listener = createWebSocketListener(
                userId = userId,
                accessToken = accessToken,
                continuation = continuation
            )

            val socket = okHttpClient.newWebSocket(
                request = request,
                listener = listener
            )

            webSocket = socket

            continuation.invokeOnCancellation {
                socket.cancel()

                if (
                    _connectionState.value ==
                    CallSocketConnectionState.CONNECTING
                ) {
                    _connectionState.value =
                        CallSocketConnectionState.DISCONNECTED
                }

                clearSocket(socket)
            }
        }
    }

    override suspend fun disconnect() {
        val socket = webSocket

        if (socket == null) {
            _connectionState.value =
                CallSocketConnectionState.DISCONNECTED
            return
        }

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

        clearSocket(socket)

        _connectionState.value =
            CallSocketConnectionState.DISCONNECTED
    }

    private fun createWebSocketListener(
        userId: Long,
        accessToken: String,
        continuation: CancellableContinuation<Unit>
    ): WebSocketListener =
        object : WebSocketListener() {

            override fun onOpen(
                webSocket: WebSocket,
                response: Response
            ) {
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
                    failConnection(
                        webSocket = webSocket,
                        throwable = IllegalStateException(
                            "개인 알림 STOMP 연결 요청을 전송하지 못했습니다."
                        ),
                        continuation = continuation
                    )
                }
            }

            override fun onMessage(
                webSocket: WebSocket,
                text: String
            ) {
                StompFrameCodec.decode(text)
                    .forEach { frame ->
                        when (frame.command) {
                            "CONNECTED" -> subscribe(
                                webSocket = webSocket,
                                userId = userId,
                                continuation = continuation
                            )

                            "MESSAGE" -> receiveNotification(
                                webSocket = webSocket,
                                frame = frame,
                                userId = userId,
                                continuation = continuation
                            )

                            "ERROR" -> failConnection(
                                webSocket = webSocket,
                                throwable =
                                    IllegalStateException(
                                        frame.body.ifBlank {
                                            "개인 알림 STOMP 오류가 발생했습니다."
                                        }
                                    ),
                                continuation = continuation
                            )
                        }
                    }
            }

            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String
            ) {
                if (
                    _connectionState.value !=
                    CallSocketConnectionState.FAILED
                ) {
                    _connectionState.value =
                        CallSocketConnectionState.DISCONNECTED
                }

                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException(
                            reason.ifBlank {
                                "개인 알림 소켓 연결이 종료되었습니다."
                            }
                        )
                    )
                }

                clearSocket(webSocket)
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                failConnection(
                    webSocket = webSocket,
                    throwable = t,
                    continuation = continuation
                )
            }
        }

    private fun subscribe(
        webSocket: WebSocket,
        userId: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        val subscribeFrame = StompFrame(
            command = "SUBSCRIBE",
            headers = linkedMapOf(
                "id" to "incoming-call-$userId",
                "destination" to "/sub/user/$userId",
                "ack" to "auto"
            )
        )

        val isQueued = webSocket.send(
            StompFrameCodec.encode(subscribeFrame)
        )

        if (isQueued) {
            _connectionState.value =
                CallSocketConnectionState.CONNECTED

            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        } else {
            failConnection(
                webSocket = webSocket,
                throwable = IllegalStateException(
                    "개인 통화 알림 채널을 구독하지 못했습니다."
                ),
                continuation = continuation
            )
        }
    }

    private fun receiveNotification(
        webSocket: WebSocket,
        frame: StompFrame,
        userId: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        runCatching {
            parseIncomingCallNotification(
                body = frame.body,
                expectedReceiverId = userId,
                gson = gson
            )
        }.onSuccess { notification ->
            val isEmitted =
                _incomingCallNotifications.tryEmit(
                    notification
                )

            if (!isEmitted) {
                failConnection(
                    webSocket = webSocket,
                    throwable = IllegalStateException(
                        "수신 전화 알림을 처리하지 못했습니다."
                    ),
                    continuation = continuation
                )
            }
        }.onFailure { throwable ->
            failConnection(
                webSocket = webSocket,
                throwable = throwable,
                continuation = continuation
            )
        }
    }

    private fun failConnection(
        webSocket: WebSocket,
        throwable: Throwable,
        continuation: CancellableContinuation<Unit>
    ) {
        _connectionState.value =
            CallSocketConnectionState.FAILED

        if (continuation.isActive) {
            continuation.resumeWithException(throwable)
        }

        webSocket.cancel()
        clearSocket(webSocket)
    }

    private fun clearSocket(socket: WebSocket) {
        if (webSocket === socket) {
            webSocket = null
        }
    }

    private companion object {
        const val SOCKET_URL =
            "ws://50.17.53.222/ws-stomp"

        const val SOCKET_HOST = "50.17.53.222"
        const val STOMP_SUBPROTOCOL = "v12.stomp"

        const val NORMAL_CLOSURE_CODE = 1000
        const val NORMAL_CLOSURE_REASON =
            "Incoming call socket disconnected"
    }
}