package com.example.myapplication.ui.login

import com.example.myapplication.api.AuthApiService
import com.example.myapplication.dto.LoginRequest
import com.example.myapplication.network.SessionManager
import com.google.gson.JsonParser
import java.io.IOException
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : LoginRepository {
    override suspend fun login(
        email: String,
        password: String
    ): LoginOutcome {
        return try{
            val response = authApiService.login(
                LoginRequest(
                    email = email,
                    password=password
                )
            )

            val body = response.body()
            val result=body?.data

            if(
                response.isSuccessful &&
                body?.isSuccess ==true &&
                result != null
            ){
                sessionManager.updateSession(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    userId = result.userId
                )

                LoginOutcome(
                    isSuccess = true,
                    message = body.message.ifBlank {
                        "로그인에 성공했습니다."
                    }
                )
            } else {
                LoginOutcome(
                    isSuccess = false,
                    message = body?.message
                        ?.takeIf { it.isNotBlank() }
                        ?: response.errorBody()?.string()?.serverMessageOrNull()
                        ?: "로그인에 실패했습니다."
                )
            }
        } catch (exception: IOException) {
            LoginOutcome(
                isSuccess = false,
                message = "네트워크 연결을 확인해 주세요."
            )
        } catch (exception: Exception) {
            LoginOutcome(
                isSuccess = false,
                message = "로그인 처리 중 오류가 발생했습니다."
            )
        }
    }

    private fun String.serverMessageOrNull(): String? = runCatching {
        JsonParser.parseString(this)
            .asJsonObject
            .get("message")
            ?.asString
            ?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
