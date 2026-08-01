package com.example.myapplication.api

import com.example.myapplication.dto.LoginRequest
import com.example.myapplication.dto.LoginResult
import com.example.myapplication.dto.RegisterRequest
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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
}
