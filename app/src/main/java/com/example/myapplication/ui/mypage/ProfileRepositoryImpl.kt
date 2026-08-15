package com.example.myapplication.ui.mypage

import com.example.myapplication.api.AuthApiService
import com.example.myapplication.network.SessionManager
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService,
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : ProfileRepository {

    override suspend fun getProfile(): ProfileDto? {
        val userId = sessionManager.userId ?: return null
        return profileApiService.getProfile(userId).data
    }

    override suspend fun updateProfile(
        nickname: String?,
        phoneNumber: String?,
        profileImageUrl: String?,
        notificationEnabled: Boolean?
    ): Boolean {
        val userId = sessionManager.userId ?: return false

        val response = profileApiService.updateProfile(
            userId = userId,
            request = ProfileUpdateRequest(
                nickname = nickname,
                phoneNumber = phoneNumber,
                profileImageUrl = profileImageUrl,
                notificationEnabled = notificationEnabled
            )
        )
        return response.isSuccess
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
}