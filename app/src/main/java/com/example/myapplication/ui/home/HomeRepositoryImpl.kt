package com.example.myapplication.ui.home

import com.example.myapplication.network.SessionManager
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApiService: HomeApiService,
    private val sessionManager: SessionManager
) : HomeRepository {

    override suspend fun getHomeInfo(): HomeUiState {
        // 로그인 시 저장된 userId 사용 (없으면 아직 로그인 전이므로 빈 상태 반환)
        val userId = sessionManager.userId ?: return HomeUiState(isLoading = false)

        val data = homeApiService.getHomeInfo(userId).data

        return HomeUiState(
            userName = data?.nickname ?: data?.name ?: "",
            // 명세서에 '오늘의 미션' 필드가 없어 학습 일수로 임시 표기
            // TODO: 미션 관련 API/필드 확정되면 교체
            todayMission = data?.learningDays?.let { "연속 학습 ${it}일째" } ?: "",
            progress = 0f,   // TODO: 진행률 필드/API 확정되면 반영
            isLoading = false
        )
    }

    /* 임의 반환 값(더미 데이터) - API 연동으로 교체하면서 일단 주석 처리
    override suspend fun getHomeInfo(): HomeUiState {
        return HomeUiState(
            userName = "사용자 이름",
            todayMission = "수어 단어 5개 익히기",
            progress = 0.4f,
            isLoading = false
        )
    }
    */
}