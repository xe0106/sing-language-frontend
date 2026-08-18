package com.example.myapplication.dto

/**
 * 최근 연락처 목록 조회 응답 DTO
 * GET /sign/language/home/recent?limit=N 의 data 배열 요소
 */
data class HomeResponse(
    val currentDate: String?,
    val greetingMessage: String?,
    val goalTitle: String?,
    val progressPercentage: Int?
)