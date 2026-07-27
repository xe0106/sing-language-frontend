package com.example.myapplication.network

/**
 * 서버 공통 응답 래퍼 (API 명세서 기준)
 * { "isSuccess": true, "code": "COMMON200", "message": "...", "data": { ... } }
 */
data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: T? = null
)