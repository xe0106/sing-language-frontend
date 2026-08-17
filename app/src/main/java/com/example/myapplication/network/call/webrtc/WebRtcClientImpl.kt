package com.example.myapplication.network.call.webrtc

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject

class WebRtcClientImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : WebRtcClient {

    private val eglBase = EglBase.create()

    override val eglBaseContext: EglBase.Context
        get() = eglBase.eglBaseContext

    private val peerConnectionFactory: PeerConnectionFactory =
        createPeerConnectionFactory()

    private val _localVideoTrack =
        MutableStateFlow<VideoTrack?>(null)

    override val localVideoTrack =
        _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack =
        MutableStateFlow<VideoTrack?>(null)

    override val remoteVideoTrack =
        _remoteVideoTrack.asStateFlow()

    private val _events =
        MutableSharedFlow<WebRtcEvent>(
            extraBufferCapacity = 16
        )

    private val peerConnectionObserver =
        object : PeerConnection.Observer {

            override fun onIceCandidate(
                candidate: IceCandidate
            ) {
                _events.tryEmit(
                    WebRtcEvent.LocalIceCandidate(
                        candidate = candidate.sdp,
                        sdpMid = candidate.sdpMid,
                        sdpMLineIndex =
                            candidate.sdpMLineIndex
                    )
                )
            }

            override fun onConnectionChange(
                newState: PeerConnection.PeerConnectionState
            ) {
                when (newState) {
                    PeerConnection.PeerConnectionState.CONNECTED -> {
                        _events.tryEmit(WebRtcEvent.Connected)
                    }

                    PeerConnection.PeerConnectionState.DISCONNECTED,
                    PeerConnection.PeerConnectionState.CLOSED -> {
                        _events.tryEmit(WebRtcEvent.Disconnected)
                    }

                    PeerConnection.PeerConnectionState.FAILED -> {
                        _events.tryEmit(
                            WebRtcEvent.Failed(
                                reason = "PeerConnection 연결 실패"
                            )
                        )
                    }

                    PeerConnection.PeerConnectionState.NEW,
                    PeerConnection.PeerConnectionState.CONNECTING -> Unit
                }
            }

            override fun onTrack(
                transceiver: RtpTransceiver
            ) {
                val track =
                    transceiver.receiver.track()

                if (track is VideoTrack) {
                    _remoteVideoTrack.value = track
                }
            }

            override fun onSignalingChange(
                newState: PeerConnection.SignalingState
            ) = Unit

            override fun onIceConnectionChange(
                newState: PeerConnection.IceConnectionState
            ) = Unit

            override fun onIceConnectionReceivingChange(
                receiving: Boolean
            ) = Unit

            override fun onIceGatheringChange(
                newState: PeerConnection.IceGatheringState
            ) = Unit

            override fun onIceCandidatesRemoved(
                candidates: Array<out IceCandidate>
            ) = Unit

            override fun onAddStream(
                stream: MediaStream
            ) = Unit

            override fun onRemoveStream(
                stream: MediaStream
            ) = Unit

            override fun onDataChannel(
                dataChannel: DataChannel
            ) = Unit

            override fun onRenegotiationNeeded() = Unit

            override fun onAddTrack(
                receiver: RtpReceiver,
                mediaStreams: Array<out MediaStream>
            ) = Unit
        }

    override val events =
        _events.asSharedFlow()

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .createInitializationOptions()
        )

        val encoderFactory =
            DefaultVideoEncoderFactory(
                eglBaseContext,
                true,
                true
            )

        val decoderFactory =
            DefaultVideoDecoderFactory(
                eglBaseContext
            )

        return PeerConnectionFactory
            .builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private var cameraCapturer: CameraVideoCapturer? = null

    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var videoSource: VideoSource? = null

    private var audioSource: AudioSource? = null

    private var audioTrack: AudioTrack? = null

    private var peerConnection: PeerConnection? = null

    private val remoteIceMutex = Mutex()

    private val closeMutex = Mutex()

    @Volatile
    private var isClosed = false

    private val pendingRemoteIceCandidates =
        mutableListOf<IceCandidate>()

    private fun createCameraCapturer(): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(context)

        val cameraName =
            enumerator.deviceNames.firstOrNull {
                enumerator.isFrontFacing(it)
            } ?: enumerator.deviceNames.firstOrNull()
            ?: throw IllegalStateException(
                "사용 가능한 카메라가 없습니다."
            )

        return enumerator.createCapturer(
            cameraName,
            null
        ) ?: throw IllegalStateException(
            "카메라를 시작할 수 없습니다."
        )
    }

    private fun createLocalMediaTracks() {
        check(cameraCapturer == null) {
            "로컬 미디어가 이미 생성되었습니다."
        }

        val capturer = createCameraCapturer()
        val createdVideoSource =
            peerConnectionFactory.createVideoSource(false)

        val textureHelper =
            SurfaceTextureHelper.create(
                "WebRtcCaptureThread",
                eglBaseContext
            )

        capturer.initialize(
            textureHelper,
            context,
            createdVideoSource.capturerObserver
        )

        capturer.startCapture(
            VIDEO_WIDTH,
            VIDEO_HEIGHT,
            VIDEO_FPS
        )

        val createdVideoTrack =
            peerConnectionFactory.createVideoTrack(
                LOCAL_VIDEO_TRACK_ID,
                createdVideoSource
            )

        val audioConstraints = MediaConstraints()

        val createdAudioSource =
            peerConnectionFactory.createAudioSource(
                audioConstraints
            )

        val createdAudioTrack =
            peerConnectionFactory.createAudioTrack(
                LOCAL_AUDIO_TRACK_ID,
                createdAudioSource
            )

        cameraCapturer = capturer
        surfaceTextureHelper = textureHelper
        videoSource = createdVideoSource
        audioSource = createdAudioSource
        audioTrack = createdAudioTrack

        _localVideoTrack.value = createdVideoTrack
    }

    private fun IceServerConfig.toWebRtcIceServer():
            PeerConnection.IceServer {

        require(urls.isNotEmpty()) {
            "ICE 서버 URL이 비어 있습니다."
        }

        val builder =
            PeerConnection.IceServer.builder(urls)

        username
            ?.takeIf { it.isNotBlank() }
            ?.let(builder::setUsername)

        credential
            ?.takeIf { it.isNotBlank() }
            ?.let(builder::setPassword)

        return builder.createIceServer()
    }

    private fun createPeerConnection(
        iceServers: List<IceServerConfig>
    ): PeerConnection {
        val configuration =
            PeerConnection.RTCConfiguration(
                iceServers.map {
                    it.toWebRtcIceServer()
                }
            ).apply {
                sdpSemantics =
                    PeerConnection.SdpSemantics.UNIFIED_PLAN

                continualGatheringPolicy =
                    PeerConnection.ContinualGatheringPolicy
                        .GATHER_CONTINUALLY
            }

        return peerConnectionFactory.createPeerConnection(
            configuration,
            peerConnectionObserver
        ) ?: throw IllegalStateException(
            "PeerConnection을 생성하지 못했습니다."
        )
    }

    private fun requirePeerConnection(): PeerConnection =
        checkNotNull(peerConnection) {
            "WebRTC 연결이 시작되지 않았습니다."
        }

    private suspend fun flushPendingRemoteIceCandidates(
        connection: PeerConnection
    ) {
        remoteIceMutex.withLock {
            val iterator =
                pendingRemoteIceCandidates.iterator()

            while (iterator.hasNext()) {
                val candidate = iterator.next()

                check(
                    connection.addIceCandidate(candidate)
                ) {
                    "대기 중인 ICE Candidate를 적용하지 못했습니다."
                }

                iterator.remove()
            }
        }
    }

    override suspend fun start(
        iceServers: List<IceServerConfig>
    ) {
        closeMutex.withLock {
            check(!isClosed) {
                "이미 종료된 WebRtcClient는 다시 시작할 수 없습니다."
            }

            if (peerConnection != null) {
                return@withLock
            }

            createLocalMediaTracks()

            val connection =
                createPeerConnection(iceServers)

            val localVideoTrack =
                checkNotNull(_localVideoTrack.value) {
                    "로컬 비디오 트랙이 없습니다."
                }

            val localAudioTrack =
                checkNotNull(audioTrack) {
                    "로컬 오디오 트랙이 없습니다."
                }

            connection.addTrack(
                localVideoTrack,
                listOf(LOCAL_MEDIA_STREAM_ID)
            )

            connection.addTrack(
                localAudioTrack,
                listOf(LOCAL_MEDIA_STREAM_ID)
            )

            peerConnection = connection
        }
    }

    override suspend fun createOffer(): String {
        val connection = requirePeerConnection()

        val description =
            connection.createOfferAwait(
                MediaConstraints()
            )

        connection.setLocalDescriptionAwait(
            description
        )

        return description.description
    }

    override suspend fun createAnswer(): String {
        val connection = requirePeerConnection()

        val description =
            connection.createAnswerAwait(
                MediaConstraints()
            )

        connection.setLocalDescriptionAwait(
            description
        )

        return description.description
    }

    override suspend fun setRemoteOffer(sdp: String) {
        require(sdp.isNotBlank()) {
            "상대방 Offer SDP가 비어 있습니다."
        }

        val description =
            SessionDescription(
                SessionDescription.Type.OFFER,
                sdp
            )

        val connection = requirePeerConnection()

        connection.setRemoteDescriptionAwait(
            description
        )

        flushPendingRemoteIceCandidates(connection)
    }

    override suspend fun setRemoteAnswer(sdp: String) {
        require(sdp.isNotBlank()) {
            "상대방 Answer SDP가 비어 있습니다."
        }

        val description =
            SessionDescription(
                SessionDescription.Type.ANSWER,
                sdp
            )

        val connection = requirePeerConnection()

        connection.setRemoteDescriptionAwait(
            description
        )

        flushPendingRemoteIceCandidates(connection)
    }

    override suspend fun addRemoteIceCandidate(
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    ) {
        require(candidate.isNotBlank()) {
            "상대방 ICE Candidate가 비어 있습니다."
        }

        val connection = requirePeerConnection()

        val iceCandidate =
            IceCandidate(
                sdpMid,
                sdpMLineIndex,
                candidate
            )

        remoteIceMutex.withLock {
            if (connection.remoteDescription == null) {
                pendingRemoteIceCandidates += iceCandidate
                return@withLock
            }

            check(
                connection.addIceCandidate(iceCandidate)
            ) {
                "상대방 ICE Candidate를 적용하지 못했습니다."
            }
        }
    }

    override fun setMicrophoneEnabled(enabled: Boolean) {
        val track = audioTrack

        if (track == null) {
            _events.tryEmit(
                WebRtcEvent.MediaOperationFailed(
                    reason = "오디오 트랙이 생성되지 않았습니다."
                )
            )
            return
        }

        if (!track.setEnabled(enabled)) {
            _events.tryEmit(
                WebRtcEvent.MediaOperationFailed(
                    reason = "마이크 상태를 변경하지 못했습니다."
                )
            )
        }
    }

    override fun switchCamera() {
        val capturer = cameraCapturer

        if (capturer == null) {
            _events.tryEmit(
                WebRtcEvent.MediaOperationFailed(
                    reason = "카메라가 시작되지 않았습니다."
                )
            )
            return
        }

        runCatching {
            capturer.switchCamera(
                object :
                    CameraVideoCapturer.CameraSwitchHandler {

                    override fun onCameraSwitchDone(
                        isFrontCamera: Boolean
                    ) = Unit

                    override fun onCameraSwitchError(
                        errorDescription: String?
                    ) {
                        _events.tryEmit(
                            WebRtcEvent.MediaOperationFailed(
                                reason =
                                    errorDescription
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "카메라를 전환하지 못했습니다."
                            )
                        )
                    }
                }
            )
        }.onFailure { exception ->
            _events.tryEmit(
                WebRtcEvent.MediaOperationFailed(
                    reason =
                        exception.message
                            ?: "카메라를 전환하지 못했습니다."
                )
            )
        }
    }

    override suspend fun close() {
        closeMutex.withLock {
            if (isClosed) {
                return@withLock
            }

            isClosed = true

            // UI가 해제된 트랙을 참조하지 않도록 먼저 제거
            val localTrack = _localVideoTrack.value
            _localVideoTrack.value = null
            _remoteVideoTrack.value = null

            remoteIceMutex.withLock {
                pendingRemoteIceCandidates.clear()
            }

            val connection = peerConnection
            peerConnection = null

            runCatching {
                connection?.close()
            }

            runCatching {
                connection?.dispose()
            }

            val capturer = cameraCapturer
            cameraCapturer = null

            runCatching {
                capturer?.stopCapture()
            }

            runCatching {
                capturer?.dispose()
            }

            val currentAudioTrack = audioTrack
            audioTrack = null

            runCatching {
                currentAudioTrack?.dispose()
            }

            runCatching {
                localTrack?.dispose()
            }

            val currentAudioSource = audioSource
            audioSource = null

            runCatching {
                currentAudioSource?.dispose()
            }

            val currentVideoSource = videoSource
            videoSource = null

            runCatching {
                currentVideoSource?.dispose()
            }

            val textureHelper = surfaceTextureHelper
            surfaceTextureHelper = null

            runCatching {
                textureHelper?.dispose()
            }

            runCatching {
                peerConnectionFactory.dispose()
            }

            runCatching {
                eglBase.release()
            }
        }
    }

    private companion object {
        const val LOCAL_VIDEO_TRACK_ID = "local_video_track"
        const val LOCAL_AUDIO_TRACK_ID = "local_audio_track"

        const val VIDEO_WIDTH = 1280
        const val VIDEO_HEIGHT = 720
        const val VIDEO_FPS = 30
        const val LOCAL_MEDIA_STREAM_ID = "local_media_stream"
    }
}