package com.example.myapplication.ui.mypage

/**
 * 프로필 조회 응답 DTO (GET /users/{userId} 의 data)
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
    val createdAt: String?,
    val weeklyAttendance: List<WeeklyAttendanceDto>? = null
)

/** 주간 출석 기록 (월~일 7개) */
data class WeeklyAttendanceDto(
    val dayOfWeek: String?,      // "MON" ~ "SUN"
    val date: String?,           // "2026-08-17"
    val achievementRate: Int?,   // 해당 일 달성률 0~100
    val attended: Boolean?       // 출석 여부
)

/**
 * 프로필 수정 요청 (PUT /users/{userId})
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