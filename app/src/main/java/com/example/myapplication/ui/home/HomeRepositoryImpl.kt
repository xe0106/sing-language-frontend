package com.example.myapplication.ui.home

import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor() : HomeRepository {

    override suspend fun getHomeInfo(): HomeUiState {
        // TODO: API 명세 나오면 Retrofit 호출로 교체
        return HomeUiState(
            userName = "사용자 이름",
            todayMission = "수어 단어 5개 익히기",
            progress = 0.4f,
            isLoading = false
        )
    }
}