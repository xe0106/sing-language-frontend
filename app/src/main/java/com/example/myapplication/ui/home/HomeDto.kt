package com.example.myapplication.ui.home

/** 홈 화면용 사용자 정보 응답 DTO (GET /users/{userId} 의 data) */
data class HomeInfoDto(
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