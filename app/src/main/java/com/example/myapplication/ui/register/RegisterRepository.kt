package com.example.myapplication.ui.register

interface RegisterRepository {
    suspend fun uploadProfileImage(
        profileImageUri: String
    ): String?

    suspend fun nicknameCheck(
        nickname: String
    ): Boolean

    suspend fun register(
        email: String,
        password: String,
        name: String,
        profileImageUrl: String,
        nickname: String,
        gender: String,
        birthDate: String,
        phoneNumber: String
    ): Boolean
}