package com.example.myapplication.ui.home

data class HomeUiState(
    val userName: String = "",
    val todayMission: String = "",
    val progress: Float = 0f,
    val isLoading: Boolean = true
)