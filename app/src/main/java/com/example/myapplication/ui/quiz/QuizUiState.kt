package com.example.myapplication.ui.quiz

data class QuizUiState(
    val question: String = "",
    val imageRes: Int? = null,
    val options: List<String> = emptyList(),
    val progress: Float = 0f,
    val selectedOption: String? = null,
    val isLoading: Boolean = true
)