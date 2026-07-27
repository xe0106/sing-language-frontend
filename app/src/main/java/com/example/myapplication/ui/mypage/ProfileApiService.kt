package com.example.myapplication.ui.mypage

import com.example.myapplication.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * 사용자 프로필 API (명세서 6. 사용자 프로필 및 학습 관리 API)
 *   - GET /users/{userId} : 프로필 조회
 *   - PUT /users/{userId} : 프로필 수정 (닉네임/전화번호/이미지/알림설정)
 *
 * ※ 명세서에는 '회원 탈퇴' API가 없음. 탈퇴 API 나오면 여기에 추가할 것.
 */
interface ProfileApiService {

    /** 프로필 조회 */
    @GET("users/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: Long
    ): ApiResponse<ProfileDto>

    /** 프로필 수정 - 변경할 필드만 담아서 전달 (나머지는 null) */
    @PUT("users/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Long,
        @Body request: ProfileUpdateRequest
    ): ApiResponse<ProfileUpdateDto>
}