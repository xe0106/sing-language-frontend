package com.example.myapplication.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.component.QuizBackground
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun QuizScreen(
    onBackClick: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
    onQuizFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    QuizBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        .clickable {
                            if (uiState.isFinished) onQuizFinished() else onBackClick()
                        }
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

            when {
                uiState.isLoading -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    CircularProgressIndicator(color = KuitTheme.colors.main1)
                }

                uiState.errorMessage != null -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = KuitTheme.colors.black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    QuizButton(text = "다시 시도") { viewModel.loadQuizzes() }
                }

                uiState.isFinished -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    Text(
                        text = "퀴즈 완료!",
                        color = KuitTheme.colors.black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${uiState.totalCount}문제 중 ${uiState.correctCount}문제를 맞혔어요",
                        color = Color(0xFF1E2939),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 홈으로 돌아가면서 진도율 갱신을 알린다
                    QuizButton(text = "홈으로 돌아가기") { onQuizFinished() }

                    Spacer(modifier = Modifier.height(12.dp))

                    QuizButton(text = "다시 풀기") { viewModel.restart() }
                }

                else -> QuizContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun QuizContent(
    uiState: QuizUiState,
    viewModel: QuizViewModel
) {
    val quiz = uiState.currentQuiz ?: return

    LinearProgressIndicator(
        progress = { uiState.progress },
        modifier = Modifier.fillMaxWidth(),
        color = KuitTheme.colors.main1,
        trackColor = KuitTheme.colors.gray2
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${uiState.currentIndex + 1} / ${uiState.totalCount}",
            color = Color(0xFF99A1AF),
            fontSize = 12.sp
        )
        Text(
            text = "${(uiState.progress * 100).toInt()}%",
            color = Color(0xFF99A1AF),
            fontSize = 12.sp
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 수어 이미지 (서버 URL, 없거나 실패하면 기본 이미지)
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(KuitTheme.colors.white),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = quiz.imageUrl,
            contentDescription = "수어 이미지",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.img_quiz_hand),
            error = painterResource(id = R.drawable.img_quiz_hand),
            fallback = painterResource(id = R.drawable.img_quiz_hand),
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = quiz.question,
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
        lineHeight = 20.sp,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 보기 2x2
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        quiz.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowOptions.forEachIndexed { columnIndex, option ->
                    val optionIndex = rowIndex * 2 + columnIndex
                    OptionBox(
                        text = option,
                        optionIndex = optionIndex,
                        uiState = uiState,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectOption(optionIndex) }
                    )
                }
                // 보기가 홀수여도 레이아웃이 안 깨지도록
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 정답 공개 후에만 노출
    if (uiState.isAnswerRevealed) {
        Text(
            text = if (uiState.isCurrentCorrect) "정답이에요!" else "아쉬워요, 다시 확인해 보세요",
            color = if (uiState.isCurrentCorrect) Color(0xFF1F9254) else Color(0xFFD84040),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        QuizButton(
            text = if (uiState.isLastQuiz) "결과 보기" else "다음 문제"
        ) { viewModel.nextQuiz() }
    }
}

@Composable
private fun OptionBox(
    text: String,
    optionIndex: Int,
    uiState: QuizUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = uiState.selectedIndex == optionIndex
    val isCorrectAnswer = uiState.currentQuiz?.correctOptionIndex == optionIndex

    // 정답 공개 전에는 선택만 표시, 공개 후에는 정답/오답 색을 입힌다
    val backgroundColor = when {
        !uiState.isAnswerRevealed && isSelected -> Color(0xFFFFE4DA)
        uiState.isAnswerRevealed && isCorrectAnswer -> Color(0xFFD6F5E3)
        uiState.isAnswerRevealed && isSelected -> Color(0xFFFFDADA)
        else -> KuitTheme.colors.white
    }
    val borderColor = when {
        uiState.isAnswerRevealed && isCorrectAnswer -> Color(0xFF1F9254)
        uiState.isAnswerRevealed && isSelected -> Color(0xFFD84040)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (borderColor == Color.Transparent) 0.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !uiState.isAnswerRevealed) { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = KuitTheme.colors.black,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuizButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KuitTheme.colors.main1)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = KuitTheme.colors.white,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}