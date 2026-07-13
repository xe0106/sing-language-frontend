package com.example.myapplication.ui.lecture

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun LectureCard(
    modifier: Modifier= Modifier,
    lecture: Lecture
){
    Box(
        modifier=modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(KuitTheme.colors.white)
            .padding(horizontal = 16.dp, vertical = 22.dp)
    ){
        AsyncImage(
            model = lecture.thumbnailUrl,
            contentDescription = "${lecture.title} 썸네일",
            modifier = Modifier.size(76.dp),
            contentScale = ContentScale.Crop
        )
    }
}