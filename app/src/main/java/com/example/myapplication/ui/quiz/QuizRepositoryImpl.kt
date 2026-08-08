package com.example.myapplication.ui.quiz

import com.example.myapplication.api.QuizApiService
import com.example.myapplication.dto.QuizSubmitRequest
import java.io.IOException
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizApiService: QuizApiService
) : QuizRepository {

    override suspend fun getQuizzes(count: Int): Result<List<QuizItem>> {
        return try {
            val response = quizApiService.getQuizzes(count)
            val body = response.body()
            val data = body?.data

            when {
                response.code() == 401 || response.code() == 403 ->
                    Result.failure(IllegalStateException("로그인이 만료되었습니다. 다시 로그인해 주세요."))

                response.isSuccessful && body?.isSuccess == true && data != null ->
                    Result.success(
                        data.map { dto ->
                            QuizItem(
                                quizId = dto.quizId,
                                question = dto.questionText.orEmpty(),
                                imageUrl = dto.image3dUrl,
                                options = dto.options.orEmpty(),
                                // ★ 서버는 1-based(1~4). 0-based 로 변환한다.
                                //   변환 안 하면 정답이 한 칸씩 밀린다.
                                correctOptionIndex = dto.correctOptionIndex?.minus(1)
                            )
                        }
                    )

                else ->
                    Result.failure(
                        IllegalStateException(body?.message ?: "퀴즈를 불러오지 못했습니다.")
                    )
            }
        } catch (exception: IOException) {
            Result.failure(IOException("네트워크 연결을 확인해 주세요."))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun submitQuiz(quizId: Long, selectedIndex: Int): Boolean {
        return try {
            val response = quizApiService.submitQuiz(
                quizId = quizId,
                // ★ 서버가 1-based 를 쓰므로 다시 +1 해서 보낸다.
                //   TODO: 실제 호출로 확인 필요 (dto/QuizDto.kt 주석 참고)
                request = QuizSubmitRequest(selectedIndex = selectedIndex + 1)
            )
            response.isSuccessful && response.body()?.isSuccess == true
        } catch (exception: IOException) {
            false
        } catch (exception: Exception) {
            false
        }
    }
}