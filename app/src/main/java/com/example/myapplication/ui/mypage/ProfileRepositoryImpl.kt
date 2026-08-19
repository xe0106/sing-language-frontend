package com.example.myapplication.ui.mypage

import com.example.myapplication.api.AuthApiService
import com.example.myapplication.api.ImageApiService
import com.example.myapplication.network.ImagePartFactory
import com.example.myapplication.network.SessionManager
import com.google.gson.JsonParser
import java.io.IOException
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService,
    private val authApiService: AuthApiService,
    private val imageApiService: ImageApiService,
    private val imagePartFactory: ImagePartFactory,
    private val sessionManager: SessionManager
) : ProfileRepository {

    override suspend fun getProfile(): ProfileDto? {
        val userId = sessionManager.userId ?: return null
        return profileApiService.getProfile(userId).data
    }

    override suspend fun checkNickname(nickname: String): NicknameCheckOutcome {
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isBlank()) {
            return NicknameCheckOutcome(
                isAvailable = false,
                message = "닉네임을 입력해 주세요."
            )
        }

        return try {
            val response = authApiService.checkNickname(trimmedNickname)
            val body = response.body()

            if (response.isSuccessful && body?.isSuccess == true) {
                NicknameCheckOutcome(
                    isAvailable = true,
                    message = "사용 가능한 닉네임입니다."
                )
            } else {
                val serverError = response.errorBody()
                    ?.string()
                    ?.toServerErrorOrNull()
                val code = body?.code ?: serverError?.code

                NicknameCheckOutcome(
                    isAvailable = false,
                    message = when {
                        code == "MEMBER409_NICKNAME" || response.code() == 409 ->
                            "이미 사용 중인 닉네임입니다."

                        response.code() in 500..599 ->
                            "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."

                        else ->
                            serverError?.message
                                ?: body?.message?.takeIf { it.isNotBlank() }
                                ?: "닉네임 중복확인에 실패했습니다."
                    }
                )
            }
        } catch (exception: IOException) {
            NicknameCheckOutcome(
                isAvailable = false,
                message = "네트워크 연결을 확인해 주세요."
            )
        } catch (exception: Exception) {
            NicknameCheckOutcome(
                isAvailable = false,
                message = "닉네임 중복확인 중 오류가 발생했습니다."
            )
        }
    }

    override suspend fun uploadProfileImage(profileImageUri: String): String? {
        return try {
            val imagePart = imagePartFactory.create(profileImageUri) ?: return null
            val response = imageApiService.uploadImage(imagePart)
            val body = response.body()

            if (response.isSuccessful && body?.isSuccess == true) {
                body.data?.imageUrl
            } else {
                null
            }
        } catch (exception: IOException) {
            null
        } catch (exception: SecurityException) {
            null
        } catch (exception: IllegalArgumentException) {
            null
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun updateProfile(
        nickname: String?,
        gender: String?,
        birthDate: String?,
        phoneNumber: String?,
        profileImageUrl: String?
    ): ProfileUpdateOutcome {
        return try {
            val response = authApiService.modifyProfile(
                ProfileUpdateRequest(
                    profileImageUrl = profileImageUrl,
                    nickname = nickname,
                    gender = gender,
                    birthDate = birthDate,
                    phoneNumber = phoneNumber
                )
            )
            val body = response.body()

            if (response.isSuccessful && body?.isSuccess == true) {
                ProfileUpdateOutcome(isSuccess = true)
            } else {
                val serverError = response.errorBody()
                    ?.string()
                    ?.toServerErrorOrNull()
                val code = body?.code ?: serverError?.code

                ProfileUpdateOutcome(
                    isSuccess = false,
                    message = when (code) {
                        "COMMON403" -> "로그인이 만료되었습니다. 다시 로그인해 주세요."
                        "MEMBER404_DELETED" -> "탈퇴한 회원입니다."
                        "MEMBER404" -> "존재하지 않는 회원입니다."
                        "MEMBER409_NICKNAME" -> "이미 사용 중인 닉네임입니다."
                        "MEMBER400_FORMAT" -> serverError?.message
                            ?: body?.message?.takeIf { it.isNotBlank() }
                            ?: "입력한 정보를 확인해 주세요."

                        else -> when (response.code()) {
                            400 -> "입력한 정보를 확인해 주세요."
                            403 -> "로그인이 만료되었습니다. 다시 로그인해 주세요."
                            404 -> "회원 정보를 찾을 수 없습니다."
                            409 -> "이미 사용 중인 닉네임입니다."
                            in 500..599 -> "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."
                            else -> "프로필 수정에 실패했습니다."
                        }
                    }
                )
            }
        } catch (exception: IOException) {
            ProfileUpdateOutcome(
                isSuccess = false,
                message = "네트워크 연결을 확인해 주세요."
            )
        } catch (exception: Exception) {
            ProfileUpdateOutcome(
                isSuccess = false,
                message = "프로필 수정 중 오류가 발생했습니다."
            )
        }
    }

    /**
     * 로그아웃.
     * 서버 호출이 실패하더라도 로컬 세션은 반드시 정리한다.
     * (토큰이 남으면 로그인 화면으로 갔다가 다시 들어와지는 문제가 생김)
     */
    override suspend fun logout() {
        runCatching { authApiService.logout() }
        sessionManager.clear()
    }

    /** 회원 탈퇴. 서버 성공을 확인한 뒤에만 세션을 정리한다. */
    override suspend fun withdraw(): Boolean {
        val response = authApiService.withdraw()

        val success = response.isSuccessful && response.body()?.isSuccess == true
        if (success) {
            sessionManager.clear()
        }
        return success
    }

    private fun String.toServerErrorOrNull(): ServerError? = runCatching {
        val json = JsonParser.parseString(this).asJsonObject
        ServerError(
            code = json.get("code")
                ?.takeUnless { it.isJsonNull }
                ?.asString,
            message = json.get("message")
                ?.takeUnless { it.isJsonNull }
                ?.asString
                ?.takeIf { it.isNotBlank() }
        )
    }.getOrNull()

    private data class ServerError(
        val code: String?,
        val message: String?
    )
}
