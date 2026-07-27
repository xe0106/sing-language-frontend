package com.example.myapplication.ui.quiz

interface QuizRepository {
    /** 퀴즈 목록 조회 (기본 5문제) */
    suspend fun getQuizzes(count: Int = 5): List<QuizUiState>
}