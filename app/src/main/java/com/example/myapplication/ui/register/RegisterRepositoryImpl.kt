package com.example.myapplication.ui.register

import com.example.myapplication.R
import com.example.myapplication.api.AuthApiService
import com.example.myapplication.api.ImageApiService
import com.example.myapplication.dto.RegisterRequest
import com.example.myapplication.network.ImagePartFactory
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class RegisterRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val imageApiService: ImageApiService,
    private val imagePartFactory: ImagePartFactory
): RegisterRepository{

    override suspend fun uploadProfileImage(
        profileImageUri: String?
    ): String? = withContext(Dispatchers.IO){
        try {
            val imagePart =
                if (profileImageUri.isNullOrBlank()) {
                    imagePartFactory.createFromResource(
                        imageResourceId = R.drawable.basic_profile2
                    )
                } else {
                    imagePartFactory.create(
                        imageUri = profileImageUri
                    ) ?: return@withContext null
                }

            val response = imageApiService.uploadImage(
                file = imagePart
            )

            val responseBody = response.body()

            if (
                response.isSuccessful &&
                responseBody?.isSuccess == true
            ) {
                responseBody.data?.imageUrl
            } else {
                null
            }
        } catch (exception: IOException) {
            null
        } catch (exception: SecurityException) {
            null
        } catch (exception: IllegalArgumentException) {
            null
        }
    }

    override suspend fun nicknameCheck(
        nickname: String
    ): Boolean {
        //return nickname.isNotBlank()

        val trimmedNickname=nickname.trim()

        if(trimmedNickname.isBlank()) {
            return false
        }

        return try {
            val response = authApiService.checkNickname(
                nickname=trimmedNickname
            )

            response.isSuccessful &&
                    response.body()?.isSuccess ==true
        } catch (exception: IOException) {
            false
        } catch (exception: Exception) {
            false
        }
    }

    override suspend fun register(
         email: String,
         password: String,
         name: String,
         profileImageUrl: String,
         nickname: String,
         gender: String,
         birthDate: String,
         phoneNumber: String
    ): RegisterOutcome {
        //return name.isNotBlank()&&email.isNotBlank()&&password.isNotBlank()

        return try {
            val response = authApiService.register(
                RegisterRequest(
                    email = email,
                    password=password,
                    name=name,
                    profileImageUrl=profileImageUrl,
                    nickname=nickname,
                    gender=gender,
                    birthDate=birthDate,
                    phoneNumber=phoneNumber
                )
            )

            val body = response.body()

            if (response.isSuccessful && body?.isSuccess == true) {
                RegisterOutcome(isSuccess = true)
            } else {
                val serverError = response.errorBody()
                    ?.string()
                    ?.toServerRegisterErrorOrNull()

                RegisterOutcome(
                    isSuccess = false,
                    message = registerErrorMessage(
                        code = body?.code ?: serverError?.code,
                        serverMessage = body?.message
                            ?.takeIf { it.isNotBlank() }
                            ?: serverError?.message,
                        httpCode = response.code()
                    )
                )
            }
        } catch (exception: IOException) {
            RegisterOutcome(
                isSuccess = false,
                message = "네트워크 연결을 확인해 주세요."
            )
        } catch (exception: Exception) {
            RegisterOutcome(
                isSuccess = false,
                message = "회원가입 처리 중 오류가 발생했습니다."
            )
        }
    }

    private fun String.toServerRegisterErrorOrNull(): ServerRegisterError? =
        runCatching {
            val json = JsonParser.parseString(this).asJsonObject

            ServerRegisterError(
                code = json.get("code")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString,
                message = json.get("message")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString
                    ?.takeIf { it.isNotBlank() }
            )
        }.getOrNull()

    private fun registerErrorMessage(
        code: String?,
        serverMessage: String?,
        httpCode: Int
    ): String = when (code) {
        "MEMBER400_FORMAT" ->
            serverMessage ?: "필수 입력값을 확인해 주세요."

        "MEMBER409_EMAIL" ->
            "이미 가입된 이메일입니다."

        "MEMBER409_NICKNAME" ->
            "이미 사용 중인 닉네임입니다."

        "MEMBER409_PHONE_NUMBER" ->
            "이미 사용 중인 전화번호입니다."

        else -> when (httpCode) {
            400 -> "입력한 회원정보를 확인해 주세요."
            409 -> "이미 사용 중인 회원정보가 있습니다."
            429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
            in 500..599 -> "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."
            else -> "회원가입에 실패했습니다."
        }
    }

    private data class ServerRegisterError(
        val code: String?,
        val message: String?
    )
}
