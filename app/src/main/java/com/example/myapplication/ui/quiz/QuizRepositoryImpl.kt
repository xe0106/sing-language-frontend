package com.example.myapplication.ui.quiz

import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizApiService: QuizApiService
) : QuizRepository {

    override suspend fun getQuizzes(count: Int): List<QuizUiState> {
        val list = quizApiService.getQuizzes(count).data ?: emptyList()

        return list.map { dto ->
            QuizUiState(
                quizId = dto.quizId,
                question = dto.questionText ?: "",
                image3dUrl = dto.image3dUrl,
                options = dto.options ?: emptyList(),
                correctOptionIndex = dto.correctOptionIndex,
                progress = 0f,
                isLoading = false
            )
        }
    }

    /* 임의 반환 값(더미 데이터) - API 연동으로 교체하면서 일단 주석 처리
    override suspend fun getQuizzes(count: Int): List<QuizUiState> {
        return listOf(
            QuizUiState(
                question = "이 수어는 무엇을 뜻할까요?",
                imageRes = R.drawable.img_quiz_hand,
                options = listOf("안녕", "고맙습니다", "사랑합니다", "미안합니다"),
                progress = 0.4f,
                isLoading = false
            )
        )
    }
    */
}