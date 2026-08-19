package com.example.myapplication.ui.mypage

interface ProfileRepository {

    /** 프로필 조회 */
    suspend fun getProfile(): ProfileDto?

    /** 프로필 수정 화면의 닉네임 중복 확인 */
    suspend fun checkNickname(nickname: String): NicknameCheckOutcome

    /** 선택한 프로필 이미지 업로드 */
    suspend fun uploadProfileImage(profileImageUri: String): String?

    /** 프로필 수정 - 변경하지 않을 값은 null 로 전달 */
    suspend fun updateProfile(
        nickname: String?,
        gender: String?,
        birthDate: String?,
        phoneNumber: String?,
        profileImageUrl: String?
    ): ProfileUpdateOutcome

    /** 로그아웃 */
    suspend fun logout()

    /** 회원 탈퇴 */
    suspend fun withdraw(): Boolean
}

data class NicknameCheckOutcome(
    val isAvailable: Boolean,
    val message: String
)

data class ProfileUpdateOutcome(
    val isSuccess: Boolean,
    val message: String? = null
)
