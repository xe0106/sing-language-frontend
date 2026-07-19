package com.example.myapplication.ui.register

interface RegisterRepository {
    suspend fun nicknameCheck(
        nickname: String
    ): Boolean

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Boolean
}