package com.example.myapplication.ui.mypage

import com.example.myapplication.network.SessionManager
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService,
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
}