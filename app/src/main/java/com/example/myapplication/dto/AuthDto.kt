package com.example.myapplication.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResult(
    val userName: String,
    val grantType: String,
    val userId: Long,
    val accessToken: String,
    val refreshToken: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val profileImageUrl: String,
    val nickname: String,
    val gender: String,
    val birthDate: String,
    val phoneNumber: String
)