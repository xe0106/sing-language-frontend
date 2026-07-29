package com.example.myapplication.ui.quiz

/**
 * 퀴즈 문제 DTO (GET /quizzes 응답의 data 배열 요소)
 * - image3dUrl: 3D 수어 모델(.glb) URL
 * - correctOptionIndex: 정답 보기 인덱스 (0~3)
 */
data class QuizDto(
    val quizId: Long,
    val questionText: String?,
    val targetSignId: Long?,
    val image3dUrl: String?,
    val options: List<String>?,
    val correctOptionIndex: Int?
)

/** 퀴즈 답안 제출 요청 (POST /quizzes/{quizId}/submit) */
data class QuizSubmitRequest(
    val userId: Long,
    val selectedIndex: Int
)

/** 퀴즈 채점 응답 DTO */
data class QuizSubmitDto(
    val quizId: Long?,
    val isCorrect: Boolean?,
    val correctOptionIndex: Int?,
    val explanation: String?
)