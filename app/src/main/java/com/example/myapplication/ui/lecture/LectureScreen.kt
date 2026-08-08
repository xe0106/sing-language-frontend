package com.example.myapplication.ui.lecture

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.component.TopBar
import com.example.myapplication.ui.lecture.component.GenreBox
import com.example.myapplication.ui.lecture.component.LectureCard
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun LectureScreen(
    modifier: Modifier =Modifier,
    viewModel: LectureViewModel= hiltViewModel(),
    onLectureClick: (Long) -> Unit
){
    val uiState=viewModel.uiState

    Box(
        modifier=modifier.fillMaxSize()
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
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = viewModel::onCategoryClick
            )

            Spacer(modifier=Modifier.height(38.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uiState.errorMessage,
                                    color = KuitTheme.colors.black
                                )

                                TextButton(
                                    onClick = viewModel::retryLectures
                                ) {
                                    Text(text = "다시 시도")
                                }
                            }
                        }
                    }

                    uiState.lectures.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "강의가 없습니다.",
                                color = KuitTheme.colors.black
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(uiState.lectures) { lecture ->
                                LectureCard(
                                    lecture = lecture,
                                    onClick = {
                                        onLectureClick(lecture.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}