package com.example.myapplication.ui.quiz

import com.example.myapplication.R
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor() : QuizRepository {

    override suspend fun getQuiz(): QuizUiState {
        // TODO: API 명세 나오면 Retrofit 호출로 교체
        // (서버 연동 시 이미지는 리소스 id 대신 이미지 URL로 바뀔 예정)
        return QuizUiState(
            question = "이 수어는 무엇을 뜻할까요?",
            imageRes = R.drawable.img_quiz_hand,
            options = listOf("안녕", "고맙습니다", "사랑합니다", "미안합니다"),
            progress = 0.4f,
            isLoading = false
        )
    }
}