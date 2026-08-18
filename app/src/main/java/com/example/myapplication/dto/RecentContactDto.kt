package com.example.myapplication.dto

/**
 * 최근 연락처 목록 조회 응답 DTO
 * GET /sign/language/home/recent?limit=N 의 data 배열 요소
 */
data class RecentContactDto(
    val contactId: Long,
    val targetUserId: Long,
    val contactName: String,
    val profileImageUrl: String?,
    val createdAt: String?,
    val lastContactedAt: String?
)