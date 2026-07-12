
package com.example.myapplication.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun QuizScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFDBDB),
                        Color(0xFFFFF3E0)
                    )
                )
            )
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
                modifier = Modifier.size(width = 10.dp, height = 19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "수어 퀴즈",
                color = KuitTheme.colors.black,
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 진행률 바
        LinearProgressIndicator(
            progress = { 0f },
            modifier = Modifier.fillMaxWidth(),
            color = KuitTheme.colors.main1,
            trackColor = KuitTheme.colors.gray2
        )
        Spacer(modifier = Modifier.height(4.dp))
        // % 빈칸
        Text(
            text = "",
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
            // 손 모양 이미지 빈칸
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 질문 텍스트
        Text(
            text = "이 수어는 무엇을 뜻할까요?",
            color = Color(0xFF1E2939),
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            lineHeight = 28.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "표현을 잘 보고 알맞은 단어를 선택하세요",
            color = Color(0xFF99A1AF),
            fontSize = 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 보기 버튼 2x2
        val options = listOf("", "", "", "")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowOptions.forEach { option ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KuitTheme.colors.white)
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


