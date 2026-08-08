package com.example.myapplication.ui.home

import com.example.myapplication.api.HomeApiService
import java.io.IOException
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApiService: HomeApiService
) : HomeRepository {

    override suspend fun getHome(): HomeUiState {
        return try {
            val response = homeApiService.getHome()
            val body = response.body()
            val data = body?.data

            when {
                response.code() == 401 || response.code() == 403 ->
                    HomeUiState(
                        isLoading = false,
                        errorMessage = "로그인이 만료되었습니다. 다시 로그인해 주세요."
                    )

                response.isSuccessful && body?.isSuccess == true && data != null ->
                    HomeUiState(
                        currentDate = data.currentDate.orEmpty(),
                        greetingMessage = data.greetingMessage.orEmpty(),
                        goalTitle = data.goalTitle.orEmpty(),
                        // 서버는 0~100 정수. Compose 진행바는 0f~1f 이므로 변환 + 범위 보정
                        progress = (data.progressPercentage ?: 0).coerceIn(0, 100) / 100f,
                        isLoading = false
                    )

                else ->
                    HomeUiState(
                        isLoading = false,
                        errorMessage = body?.message ?: "홈 정보를 불러오지 못했습니다."
                    )
            }
        } catch (exception: IOException) {
            HomeUiState(
                isLoading = false,
                errorMessage = "네트워크 연결을 확인해 주세요."
            )
        } catch (exception: Exception) {
            HomeUiState(
                isLoading = false,
                errorMessage = "홈 정보를 불러오지 못했습니다."
            )
        }
    }
}