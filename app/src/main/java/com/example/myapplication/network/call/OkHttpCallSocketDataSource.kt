package com.example.myapplication.network.call

import com.example.myapplication.dto.CallSocketMessageDto
import com.example.myapplication.dto.CallSocketMessageType
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

    private var webSocket: WebSocket? = null
    private var currentCallId: String? = null

    override suspend fun connectAndSubscribe(
        callId: String
    ) {
        check(
            _connectionState.value !=
                CallSocketConnectionState.CONNECTING &&
                _connectionState.value !=
                CallSocketConnectionState.CONNECTED
        ) {
            "이미 통화 소켓이 연결 중이거나 연결되어 있습니다."
        }

        val accessToken = sessionManager.accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "로그인 토큰이 없습니다."
            )

        currentCallId = callId
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
                callId = callId,
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

    override suspend fun send(
        message: CallSocketMessageDto
    ) {
        check(
            _connectionState.value ==
                    CallSocketConnectionState.CONNECTED
        ) {
            "통화 소켓이 연결되어 있지 않습니다."
        }

        val socket = checkNotNull(webSocket) {
            "사용할 수 있는 WebSocket이 없습니다."
        }

        val connectedCallId = checkNotNull(currentCallId) {
            "연결된 통화 정보가 없습니다."
        }

        require(message.callId == connectedCallId) {
            "현재 연결된 통화와 메시지의 callId가 다릅니다."
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
            _connectionState.value = CallSocketConnectionState.FAILED

            socket.cancel()
            clearSocket(socket)

            throw IllegalStateException(
                "소켓 메시지를 전송하지 못했습니다."
            )
        }
    }

    override suspend fun disconnect() {
        val socket = webSocket

        if(socket == null) {
            currentCallId = null
            _connectionState.value = CallSocketConnectionState.DISCONNECTED
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

        _connectionState.value = CallSocketConnectionState.DISCONNECTED
    }

    private fun createWebSocketListener(
        callId: String,
        accessToken: String,
        continuation: CancellableContinuation<Unit>
    ): WebSocketListener = object : WebSocketListener() {

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
                        "STOMP 연결 요청을 전송하지 못했습니다."
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
                            callId = callId,
                            continuation = continuation
                        )

                        "MESSAGE" -> receiveMessage(
                            webSocket = webSocket,
                            frame = frame,
                            continuation = continuation
                        )

                        "ERROR" -> failConnection(
                            webSocket = webSocket,
                            throwable =
                                IllegalStateException(
                                    frame.body.ifBlank {
                                        "STOMP 연결에 실패했습니다."
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
                            "통화 소켓 연결이 종료되었습니다."
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
        callId: String,
        continuation: CancellableContinuation<Unit>
    ) {
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
                    "통화방 구독 요청을 전송하지 못했습니다."
                ),
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

    private fun receiveMessage(
        webSocket: WebSocket,
        frame: StompFrame,
        continuation: CancellableContinuation<Unit>
    ) {
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
                failConnection(
                    webSocket = webSocket,
                    throwable = IllegalStateException(
                        "수신한 소켓 메시지를 처리하지 못했습니다."
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

    private fun clearSocket(socket: WebSocket) {
        if (webSocket === socket) {
            webSocket = null
            currentCallId = null
        }
    }

    private companion object {
        const val SOCKET_URL =
            "ws://50.17.53.222/ws-stomp"
        const val SOCKET_HOST = "50.17.53.222"
        const val STOMP_SUBPROTOCOL = "v12.stomp"
        const val SIGNAL_DESTINATION = "/pub/call/signal"

        const val NORMAL_CLOSURE_CODE = 1000
        const val NORMAL_CLOSURE_REASON = "Call socket disconnected"
    }
}
