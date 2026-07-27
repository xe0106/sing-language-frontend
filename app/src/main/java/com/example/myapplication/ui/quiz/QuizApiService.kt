package com.example.myapplication.ui.quiz

import com.example.myapplication.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 수어 퀴즈 API (명세서 3. 퀴즈 API)
 *   - GET  /quizzes?count=N          : 퀴즈 목록 조회
 *   - POST /quizzes/{quizId}/submit  : 답안 제출/채점
 */
interface QuizApiService {

    /** 퀴즈 목록 조회 (count = 출제 개수, 기본 5) */
    @GET("quizzes")
    suspend fun getQuizzes(
        @Query("count") count: Int = 5
    ): ApiResponse<List<QuizDto>>

    /** 퀴즈 답안 제출 및 채점 */
    @POST("quizzes/{quizId}/submit")
    suspend fun submitQuiz(
        @Path("quizId") quizId: Long,
        @Body request: QuizSubmitRequest
    ): ApiResponse<QuizSubmitDto>
}