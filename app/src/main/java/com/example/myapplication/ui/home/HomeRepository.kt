package com.example.myapplication.ui.home

interface HomeRepository {
    suspend fun getHomeInfo(): HomeUiState
}