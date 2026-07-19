package com.example.myapplication.ui.quiz

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.component.QuizBackground
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun QuizScreen(
    onBackClick: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    QuizBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 뒤로가기 + 제목
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
                    text = "수어 퀴즈",
                    color = KuitTheme.colors.black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 진행률 바 (TODO: 서버 연동 시 ViewModel 값으로 교체)
            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier.fillMaxWidth(),
                color = KuitTheme.colors.main1,
                trackColor = KuitTheme.colors.gray2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "40%",
                color = Color(0xFF99A1AF),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 손 모양 원형
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(KuitTheme.colors.white),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_quiz_hand),
                    contentDescription = "수어 이미지",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 질문 텍스트
            Text(
                text = "이 수어는 무엇을 뜻할까요?",
                color = Color(0xFF1E2939),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "표현을 잘 보고 알맞은 단어를 선택하세요",
                color = Color(0xFF99A1AF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 보기 버튼 2x2 (TODO: 서버 연동 시 ViewModel 값으로 교체)
            val options = uiState.options
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.chunked(2).forEach { rowOptions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowOptions.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (uiState.selectedOption == option) Color(0xFFFFE4DA)
                                        else KuitTheme.colors.white
                                    )
                                    .clickable { viewModel.selectOption(option) }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    color = KuitTheme.colors.black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    QuizScreen()
}