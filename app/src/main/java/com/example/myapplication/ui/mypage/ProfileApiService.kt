package com.example.myapplication.ui.mypage

import com.example.myapplication.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 사용자 프로필 API
 *   - GET /sign/language/users/{userId} : 프로필 조회
 *
 * ※ 회원 정보 수정/로그아웃/회원탈퇴는 AuthApiService 에 있음 (auth 계열 경로)
 */
interface ProfileApiService {

    /** 프로필 조회 */
    @GET("sign/language/users/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: Long
    ): ApiResponse<ProfileDto>
}
