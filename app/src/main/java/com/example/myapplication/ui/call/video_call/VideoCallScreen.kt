package com.example.myapplication.ui.call.video_call

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.ui.call.video_call.component.CallControlBar
import com.example.myapplication.ui.call.video_call.component.CallMessagePanel
import com.example.myapplication.ui.call.video_call.component.CallingInformation
import com.example.myapplication.ui.call.video_call.component.LocalVideoView
import com.example.myapplication.ui.call.video_call.component.RemoteVideoView
import org.webrtc.EglBase
import org.webrtc.VideoTrack

@Composable
fun VideoCallScreen(
    callId: String,
    onCallEnded: () -> Unit,
    modifier: Modifier= Modifier,
    viewModel: VideoCallViewModel = hiltViewModel()
){
    val uiState=viewModel.uiState

    val localVideoTrack by
    viewModel.localVideoTrack.collectAsState()

    val remoteVideoTrack by
    viewModel.remoteVideoTrack.collectAsState()

    LaunchedEffect(callId) {
        viewModel.loadCall(callId)
    }

    LaunchedEffect(uiState.connectionState) {
        if(uiState.connectionState == CallConnectionState.ENDED){
            onCallEnded()
        }
    }

    BackHandler(
        enabled =
            uiState.connectionState !=
                    CallConnectionState.ENDED
    ) {
        if (uiState.callId == null) {
            onCallEnded()
        } else {
            viewModel.endCall()
        }
    }

    VideoCallContent(
        uiState = uiState,
        localVideoTrack = localVideoTrack,
        remoteVideoTrack = remoteVideoTrack,
        eglBaseContext = viewModel.eglBaseContext,
        onMessageChange = viewModel::updateMessage,
        onSendMessage = viewModel::sendMessage,
        onToggleMic = viewModel::toggleMic,
        onSwitchCamera = viewModel::switchCamera,
        onEndCall = viewModel::endCall,
        modifier = modifier
    )
}

@Composable
fun VideoCallContent(
    uiState: VideoCallUiState,
    localVideoTrack: VideoTrack?,
    remoteVideoTrack: VideoTrack?,
    eglBaseContext: EglBase.Context,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onToggleMic: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier=modifier.fillMaxSize()
    ){
        LocalVideoView(
            videoTrack = localVideoTrack,
            eglBaseContext = eglBaseContext,
            modifier = Modifier.fillMaxSize()
        )

        when (uiState.connectionState) {
            CallConnectionState.CALLING,
                CallConnectionState.CONNECTING -> {
                    CallingInformation(
                        name = uiState.remoteName,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top=72.dp)
                    )
                }

            CallConnectionState.CONNECTED -> {
                RemoteVideoView(
                    videoTrack = remoteVideoTrack,
                    eglBaseContext = eglBaseContext,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 44.dp, end = 19.dp)
                        .size(width = 111.dp, height = 163.dp)
                        .clip(RoundedCornerShape(24.dp))
                )

                CallMessagePanel(
                    messages = uiState.messages,
                    input = uiState.messageInput,
                    onInputChange = onMessageChange,
                    onSendClick = onSendMessage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(start=16.dp, end=16.dp, bottom=79.dp)
                )
            }

            else -> Unit
        }

        CallControlBar(
            isMicEnabled = uiState.isMicEnabled,
            isCameraReady = uiState.isLocalVideoReady,
            onToggleMic=onToggleMic,
            onSwitchCamera = onSwitchCamera,
            onEndCall=onEndCall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp)
        )
    }
}
