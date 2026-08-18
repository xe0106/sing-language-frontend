package com.example.myapplication.ui.home

import com.example.myapplication.dto.RecentContactDto

interface HomeRepository {
    /** 홈 화면 정보 조회. 실패해도 예외를 던지지 않고 errorMessage 가 채워진 상태를 반환한다. */
    suspend fun getHome(): HomeUiState

    /** 최근 연락처 목록 조회. 실패 시 빈 리스트를 반환한다. */
    suspend fun getRecentContacts(limit: Int = 3): List<RecentContactDto>
}