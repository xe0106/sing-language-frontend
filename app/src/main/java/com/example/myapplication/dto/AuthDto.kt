package com.example.myapplication.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResult(
    val userName: String,
    val grantType: String,
    val accessToken: String,
    val refreshToken: String
)
