package com.example.myapplication.ui.mypage

/**
 * 프로필 조회 응답 DTO (GET /users/{userId} 의 data)
 * gender 는 서버에서 "male" / "female" 문자열로 내려옴
 */
data class ProfileDto(
    val userId: Long?,
    val email: String?,
    val name: String?,
    val nickname: String?,
    val gender: String?,
    val birthDate: String?,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val learningDays: Int?,
    val notificationEnabled: Boolean?,
    val createdAt: String?
)

/**
 * 프로필 수정 요청 (PUT /users/{userId})
 * 명세서 기준 수정 가능 필드: nickname, phoneNumber, profileImageUrl, notificationEnabled
 * - 변경하지 않을 필드는 null 로 두면 됨
 */
data class ProfileUpdateRequest(
    val nickname: String? = null,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val notificationEnabled: Boolean? = null
)

/** 프로필 수정 응답 DTO */
data class ProfileUpdateDto(
    val userId: Long?,
    val nickname: String?,
    val phoneNumber: String?,
    val notificationEnabled: Boolean?,
    val updatedAt: String?
)