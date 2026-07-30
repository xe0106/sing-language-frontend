package com.example.myapplication.ui.call.video_call.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallingInformation(
    name: String,
    modifier: Modifier=Modifier
){
    Column(
        modifier=modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = name,
            color = KuitTheme.colors.white,
            style = KuitTheme.typography.B_24
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "영상 통화 연결 중...",
            color = KuitTheme.colors.white,
            style = KuitTheme.typography.R_16
        )
    }
}