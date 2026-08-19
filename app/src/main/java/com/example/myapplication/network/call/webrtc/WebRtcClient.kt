package com.example.myapplication.network.call.webrtc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.example.myapplication.ui.call.video_call.sign.SignFeatureFrame
import org.webrtc.EglBase
import org.webrtc.VideoTrack

interface WebRtcClient {

    val eglBaseContext: EglBase.Context

    val localVideoTrack: StateFlow<VideoTrack?>

    val remoteVideoTrack: StateFlow<VideoTrack?>

    /** 목표 FPS로 로컬 카메라에서 추출한 258개 수어 특징. */
    val localSignFeatures: Flow<SignFeatureFrame>

    val events: Flow<WebRtcEvent>

    /**
     * 로컬 카메라·마이크 트랙과 PeerConnection을 생성한다.
     *
     * 카메라 및 마이크 권한이 허용된 이후 호출해야 한다.
     */
    suspend fun start(
        iceServers: List<IceServerConfig>
    )

    /**
     * 발신자가 상대방의 CONNECTED 상태를 받은 후 호출한다.
     */
    suspend fun createOffer(): String

    /**
     * 수신자가 OFFER를 적용한 후 호출한다.
     */
    suspend fun createAnswer(): String

    suspend fun setRemoteOffer(sdp: String)

    suspend fun setRemoteAnswer(sdp: String)

    suspend fun addRemoteIceCandidate(
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    )

    fun setMicrophoneEnabled(enabled: Boolean)

    fun switchCamera()

    /**
     * 카메라, 오디오, PeerConnection 및 EGL 자원을 정리한다.
     */
    suspend fun close()
}

data class IceServerConfig(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null
)

sealed interface WebRtcEvent {

    data class LocalIceCandidate(
        val candidate: String,
        val sdpMid: String?,
        val sdpMLineIndex: Int
    ) : WebRtcEvent

    data object Connected : WebRtcEvent

    data object Disconnected : WebRtcEvent

    data class Failed(
        val reason: String?
    ) : WebRtcEvent

    data class MediaOperationFailed(
        val reason: String
    ) : WebRtcEvent
}
