package com.example.myapplication.ui.quiz

interface QuizRepository {
    suspend fun getQuiz(): QuizUiState
}