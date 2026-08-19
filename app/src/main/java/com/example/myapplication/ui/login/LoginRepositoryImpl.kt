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
                val serverError = response.errorBody()
                    ?.string()
                    ?.toServerLoginErrorOrNull()

                LoginOutcome(
                    isSuccess = false,
                    message = loginErrorMessage(
                        code = body?.code ?: serverError?.code,
                        serverMessage = body?.message
                            ?.takeIf { it.isNotBlank() }
                            ?: serverError?.message,
                        httpCode = response.code()
                    )
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

    private fun String.toServerLoginErrorOrNull(): ServerLoginError? =
        runCatching {
            val json = JsonParser.parseString(this).asJsonObject

            ServerLoginError(
                code = json.get("code")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString,
                message = json.get("message")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString
                    ?.takeIf { it.isNotBlank() }
            )
        }.getOrNull()

    private fun loginErrorMessage(
        code: String?,
        serverMessage: String?,
        httpCode: Int
    ): String = when (code) {
        "MEMBER400_FORMAT" ->
            serverMessage ?: "이메일과 비밀번호를 입력해 주세요."

        "MEMBER404" ->
            "존재하지 않는 회원입니다."

        "MEMBER401" ->
            "비밀번호가 일치하지 않습니다."

        else -> when (httpCode) {
            400 -> "입력한 정보를 확인해 주세요."
            401 -> "이메일 또는 비밀번호가 올바르지 않습니다."
            404 -> "존재하지 않는 회원입니다."
            429 -> "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요."
            in 500..599 -> "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."
            else -> "로그인에 실패했습니다."
        }
    }

    private data class ServerLoginError(
        val code: String?,
        val message: String?
    )
}
