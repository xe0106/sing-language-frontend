package com.example.myapplication.ui.lecture

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.component.TopBar

@Composable
fun LectureScreen(
    modifier: Modifier =Modifier
){
    val genres=listOf(
        Genre("인간"),
        Genre("삶"),
        Genre("식생활"),
        Genre("의생활"),
        Genre("주생활"),
        Genre("사회생활"),
        Genre("경제생활"),
        Genre("교육"),
        Genre("나라명 및 지명"),
        Genre("종교"),
        Genre("문화"),
        Genre("정치와 행정"),
        Genre("자연"),
        Genre("동식물"),
        Genre("개념"),
        Genre("기타"),
    )

    var selectedGenre by remember{ mutableStateOf<Genre>(genres.first()) }

    Box(
        modifier=Modifier.fillMaxSize()
    ){
        Image(
            painter= painterResource(id=R.drawable.lecture_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier=Modifier.height(55.dp))

            TopBar(title = "수어 강의")

            Spacer(modifier=Modifier.height(8.dp))

            GenreBox(
                genres=genres,
                selectedGenre = selectedGenre,
                onGenreClick = {clickedGenre->
                    selectedGenre=clickedGenre
                }
            )
        }
    }
}