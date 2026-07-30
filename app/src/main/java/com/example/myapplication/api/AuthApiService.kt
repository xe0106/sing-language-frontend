package com.example.myapplication.api

import com.example.myapplication.dto.LoginRequest
import com.example.myapplication.dto.LoginResult
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("sign/language/auth/signin")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResult>>
}
