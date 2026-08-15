package com.example.myapplication.ui.mypage

interface ProfileRepository {

    /** 프로필 조회 */
    suspend fun getProfile(): ProfileDto?

    /** 프로필 수정 - 변경하지 않을 값은 null 로 전달 */
    suspend fun updateProfile(
        nickname: String?,
        phoneNumber: String?,
        profileImageUrl: String?,
        notificationEnabled: Boolean?
    ): Boolean

    /** 로그아웃 */
    suspend fun logout()

    /** 회원 탈퇴 */
    suspend fun withdraw(): Boolean
}