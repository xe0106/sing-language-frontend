package com.example.myapplication.ui.lecture.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun LectureCard(
    modifier: Modifier= Modifier,
    lecture: Lecture,
    onClick:()->Unit
){
    Box(
        modifier=modifier
            .width(353.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KuitTheme.colors.white)
            .clickable{onClick()}
            .padding(horizontal = 16.dp, vertical = 22.dp)
    ){
        Row(
            modifier=Modifier.fillMaxSize()
        ){
            AsyncImage(
                model = lecture.thumbnailUrl,
                contentDescription = "${lecture.title} 썸네일",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier=Modifier.width(13.dp))

            LectureInfo(
                lecture = lecture
            )

            Spacer(modifier=Modifier.weight(1f))

            Image(
                painter= painterResource(id=R.drawable.lecture_play_button),
                contentDescription = null,
                modifier= Modifier.size(24.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
