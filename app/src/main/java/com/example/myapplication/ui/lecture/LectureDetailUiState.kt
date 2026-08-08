package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Lecture

data class LectureDetailUiState(
    val lecture: Lecture? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)