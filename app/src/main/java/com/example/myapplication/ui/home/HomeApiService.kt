package com.example.myapplication.ui.home

import com.example.myapplication.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 홈 화면 API
 * 명세서에 '홈' 전용 API가 없어, 홈에 필요한 사용자 정보/학습 통계는
 * 사용자 프로필 조회 API 로 가져온다.
 *   - GET /users/{userId} : 프로필 + 학습 일수(learningDays)
 */
interface HomeApiService {

    @GET("users/{userId}")
    suspend fun getHomeInfo(
        @Path("userId") userId: Long
    ): ApiResponse<HomeInfoDto>
}