package com.example.myapplication.api

import com.example.myapplication.dto.QuizResponse
import com.example.myapplication.dto.QuizSubmitRequest
import com.example.myapplication.dto.QuizSubmitResponse
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 퀴즈 API
 *   - GET  /sign/language/quizzes?count=5          : 퀴즈 목록 조회
 *   - POST /sign/language/quizzes/{quizId}/submit  : 답안 제출/채점
 *
 * Authorization: Bearer {accessToken} 필수.
 * AuthInterceptor 가 자동으로 붙여주므로 여기서 @Header 로 받지 않는다.
 */
interface QuizApiService {

    @GET("sign/language/quizzes")
    suspend fun getQuizzes(
        @Query("count") count: Int = 5
    ): Response<ApiResponse<List<QuizResponse>>>

    @POST("sign/language/quizzes/{quizId}/submit")
    suspend fun submitQuiz(
        @Path("quizId") quizId: Long,
        @Body request: QuizSubmitRequest
    ): Response<ApiResponse<QuizSubmitResponse>>
}