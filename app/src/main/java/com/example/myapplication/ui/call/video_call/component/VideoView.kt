package com.example.myapplication.ui.call.video_call.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun LocalVideoView(
    modifier: Modifier= Modifier
){
    VideoPlaceholder(
        text="내 카메라",
        modifier=modifier
    )
}

@Composable
fun RemoteVideoView(
    modifier: Modifier= Modifier
){
    VideoPlaceholder(
        text="상대방 카메라",
        modifier=modifier
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