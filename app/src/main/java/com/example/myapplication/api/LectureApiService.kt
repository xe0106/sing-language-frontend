package com.example.myapplication.api

import com.example.myapplication.dto.LectureListResponse
import com.example.myapplication.dto.LectureResponse
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LectureApiService {
    @GET("sign/language/lectures")
    suspend fun viewLectureList(
        @Query("category") category: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): Response<ApiResponse<LectureListResponse>>

    @GET("sign/language/lectures/{lectureId}")
    suspend fun viewLectureDetail(
        @Path("lectureId") lectureId: Long
    ): Response<ApiResponse<LectureResponse>>
}