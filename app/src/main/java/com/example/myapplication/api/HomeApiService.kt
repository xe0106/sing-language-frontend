package com.example.myapplication.api

import com.example.myapplication.dto.HomeResponse
import com.example.myapplication.dto.RecentContactDto
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 홈 API
 *  - GET /sign/language/home            : 홈 화면 정보 (날짜/인사말/목표/진행률)
 *  - GET /sign/language/home/recent     : 최근 연락처 목록
 *
 * Authorization: Bearer {accessToken} 필수.
 * AuthInterceptor 가 자동으로 붙여주므로 여기서 @Header 로 받지 않는다.
 */
interface HomeApiService {

    @GET("sign/language/home")
    suspend fun getHome(): Response<ApiResponse<HomeResponse>>

    /** 최근 연락처 목록 조회 */
    @GET("sign/language/home/recent")
    suspend fun getRecentContacts(
        @Query("limit") limit: Int = 3
    ): Response<ApiResponse<List<RecentContactDto>>>
}