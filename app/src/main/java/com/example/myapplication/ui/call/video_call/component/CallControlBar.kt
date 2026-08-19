package com.example.myapplication.ui.call.video_call.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallControlBar(
    isMicEnabled: Boolean,
    isCameraReady: Boolean,
    onToggleMic: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
){
    Surface(
        modifier=modifier
            .fillMaxWidth()
            .height(50.dp),
        color= KuitTheme.colors.white.copy(alpha=0.9f),
        shape = RoundedCornerShape(10000.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = onToggleMic){
                Icon(
                    painter = painterResource(
                        id= if (isMicEnabled) {
                            R.drawable.mic
                        } else {
                            R.drawable.mic_off
                        }
                    ),
                    contentDescription = "마이크",
                    tint = KuitTheme.colors.black
                )
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .background(
                        color = KuitTheme.colors.black,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.call_end),
                    contentDescription = "통화 종료",
                    tint = KuitTheme.colors.white
                )
            }

            IconButton(
                onClick = onSwitchCamera,
                enabled = isCameraReady
            ) {
                Icon(
                    painter =
                        painterResource(
                            R.drawable.camera_switch
                        ),
                    contentDescription = "카메라 전환",
                    tint =
                        if (isCameraReady) {
                            KuitTheme.colors.black
                        } else {
                            KuitTheme.colors.gray1
                        }
                )
            }
        }
    }
}
