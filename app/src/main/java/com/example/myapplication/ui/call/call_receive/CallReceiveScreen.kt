package com.example.myapplication.ui.call.call_receive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallReceiveScreen(
    callId: Long,
    modifier: Modifier =Modifier,
    viewModel: CallReceiveViewModel = hiltViewModel(),
    onAcceptSuccess: ()->Unit,
    onRejectSuccess: ()->Unit
){
    val uiState = viewModel.uiState
    val incomingCall = uiState.incomingCall

    LaunchedEffect(callId) {
        viewModel.loadIncomingCall(callId)
    }

    LaunchedEffect(uiState.isAcceptSuccess) {
        if (uiState.isAcceptSuccess) onAcceptSuccess()
    }

    LaunchedEffect(uiState.isRejectSuccess) {
        if (uiState.isRejectSuccess) onRejectSuccess()
    }

    Box(
        modifier=modifier.fillMaxSize()
    ){
        Image(
            painter= painterResource(id=R.drawable.call_receive_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier= Modifier.height(188.dp))

            AsyncImage(
                model = incomingCall?.callerProfileImageUrl,
                contentDescription = "상대 프로필 이미지",
                modifier = Modifier
                    .width(154.dp)
                    .height(144.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier= Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = incomingCall?.callerName.orEmpty(),
                    color = Color(0XFF413D3A),
                    style = KuitTheme.typography.B_20
                )
            }

            Spacer(modifier= Modifier.height(13.dp))

            Box(
                modifier = Modifier
                    .width(106.dp)
                    .height(17.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "영상 통화 수신 중...",
                    color = KuitTheme.colors.gray1,
                    style = KuitTheme.typography.R_14
                )
            }

            Spacer(modifier= Modifier.height(277.dp))

            CallReceiveButton(
                onClick = viewModel::acceptCall,
                buttonName = "수락",
                icon = R.drawable.call_receive_icon1
            )

            Spacer(modifier= Modifier.height(8.dp))

            CallReceiveButton(
                onClick = viewModel::rejectCall,
                buttonName = "거절",
                bgColor = KuitTheme.colors.white,
                textColor = KuitTheme.colors.black,
                icon = R.drawable.call_receive_icon2
            )
        }
    }
}