package com.example.myapplication.ui.home

interface HomeRepository {
    /** 홈 화면 정보 조회. 실패해도 예외를 던지지 않고 errorMessage 가 채워진 상태를 반환한다. */
    suspend fun getHome(): HomeUiState
}