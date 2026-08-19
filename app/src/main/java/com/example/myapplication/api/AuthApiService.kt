package com.example.myapplication.api

import com.example.myapplication.dto.LoginRequest
import com.example.myapplication.dto.LoginResult
import com.example.myapplication.dto.RegisterRequest
import com.example.myapplication.network.ApiResponse
import com.example.myapplication.ui.mypage.ProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("sign/language/auth/signin")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResult>>

    @GET("sign/language/auth/check-nickname")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<ApiResponse<String>>

    @POST("sign/language/auth/signup")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<String>>

    /** 회원 정보 일부 수정 - AuthInterceptor 가 액세스 토큰을 자동으로 붙여준다 */
    @PATCH("sign/language/auth/modify")
    suspend fun modifyProfile(
        @Body request: ProfileUpdateRequest
    ): Response<ApiResponse<String>>

    /** 로그아웃 - AuthInterceptor 가 토큰을 자동으로 붙여준다 */
    @POST("sign/language/auth/signout")
    suspend fun logout(): Response<ApiResponse<String>>

    /** 회원 탈퇴 */
    @DELETE("sign/language/auth/signoff")
    suspend fun withdraw(): Response<ApiResponse<String>>
}
