package com.example.myapplication.api

import com.example.myapplication.dto.HomeResponse
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * 홈 API
 *   - GET /sign/language/home : 홈 화면 정보 (날짜/인사말/목표/진행률)
 *
 * Authorization: Bearer {accessToken} 필수.
 * AuthInterceptor 가 자동으로 붙여주므로 여기서 @Header 로 받지 않는다.
 *
 * TODO: GET /sign/language/home/recent?limit=2 (최근 연락처) 는
 *       응답 형태를 아직 확인하지 못해 제외했다. 확인 후 추가할 것.
 */
interface HomeApiService {

    @GET("sign/language/home")
    suspend fun getHome(): Response<ApiResponse<HomeResponse>>
}