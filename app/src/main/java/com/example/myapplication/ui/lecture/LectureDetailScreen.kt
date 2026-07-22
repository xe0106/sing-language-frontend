package com.example.myapplication.ui.lecture

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.myapplication.R
import com.example.myapplication.ui.lecture.component.Lecture
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun LectureDetailScreen(
    lectureId: Long,
    viewModel: LectureViewModel= hiltViewModel(),
    onBackClick: ()->Unit
){
    val uiState=viewModel.uiState
    val lecture=uiState.selectedLecture

    LaunchedEffect(lectureId) {
        viewModel.loadLectureDetail(lectureId)
    }

    when{
        uiState.isDetailLoading->{
            Text(text="불러오는 중...")
        }

        uiState.detailErrorMessage !=null->{
            Text(text = uiState.detailErrorMessage)
        }

        lecture !=null ->{
            LectureDetailScreenContent(
                lecture=lecture,
                onBackClick=onBackClick
            )
        }
    }

}

@Composable
private fun LectureDetailScreenContent(
    lecture: Lecture,
    onBackClick: () -> Unit
){
    val context= LocalContext.current

    val player = remember(lecture.videoUrl){
        ExoPlayer.Builder(context).build().apply{
            setMediaItem(MediaItem.fromUri(lecture.videoUrl))
            prepare()
            playWhenReady=false
        }
    }

    DisposableEffect(player){
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter= painterResource(id=R.drawable.lecture_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Spacer(modifier = Modifier.height(55.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF000000),
                    modifier = Modifier
                        .size(width = 10.dp, height = 19.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "수어 강의",
                    color = KuitTheme.colors.black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f),
                factory = {
                    PlayerView(it).apply{
                        this.player=player
                    }
                }
            )
        }
    }
}