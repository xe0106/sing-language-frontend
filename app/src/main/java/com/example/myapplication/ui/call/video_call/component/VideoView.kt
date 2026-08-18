package com.example.myapplication.ui.call.video_call.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.ui.theme.KuitTheme
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun LocalVideoView(
    videoTrack: VideoTrack?,
    eglBaseContext: EglBase.Context,
    modifier: Modifier = Modifier
){
    if (videoTrack == null) {
        VideoPlaceholder(
            text = "내 카메라",
            modifier = modifier
        )
        return
    }

    WebRtcVideoView(
        videoTrack = videoTrack,
        eglBaseContext = eglBaseContext,
        mirror = true,
        isOverlay = false,
        modifier = modifier
    )
}

@Composable
fun RemoteVideoView(
    videoTrack: VideoTrack?,
    eglBaseContext: EglBase.Context,
    modifier: Modifier = Modifier
){
    if (videoTrack == null) {
        VideoPlaceholder(
            text = "상대방 카메라",
            modifier = modifier
        )
        return
    }

    WebRtcVideoView(
        videoTrack = videoTrack,
        eglBaseContext = eglBaseContext,
        mirror = false,
        isOverlay = true,
        modifier = modifier
    )
}

@Composable
private fun WebRtcVideoView(
    videoTrack: VideoTrack,
    eglBaseContext: EglBase.Context,
    mirror: Boolean,
    isOverlay: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val renderer =
        remember(eglBaseContext) {
            SurfaceViewRenderer(context).apply {
                init(eglBaseContext, null)
                setEnableHardwareScaler(true)
                setScalingType(
                    RendererCommon.ScalingType
                        .SCALE_ASPECT_FILL
                )
                setMirror(mirror)
                setZOrderMediaOverlay(isOverlay)
            }
        }

    // 렌더러 자체 자원 해제
    DisposableEffect(renderer) {
        onDispose {
            renderer.release()
        }
    }

    // 트랙이 바뀌면 기존 Sink 제거 후 새 트랙에 연결
    DisposableEffect(
        renderer,
        videoTrack
    ) {
        videoTrack.addSink(renderer)

        onDispose {
            videoTrack.removeSink(renderer)
        }
    }

    AndroidView(
        factory = {
            renderer
        },
        update = {
            it.setMirror(mirror)
        },
        modifier = modifier
    )
}

@Composable
private fun VideoPlaceholder(
    text: String,
    modifier: Modifier= Modifier
){
    Box(
        modifier = modifier.background(Color(0xFF242424)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = KuitTheme.typography.R_14
        )
    }
}