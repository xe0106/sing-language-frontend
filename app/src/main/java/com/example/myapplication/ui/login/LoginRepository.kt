package com.example.myapplication.ui.login

interface LoginRepository {
    suspend fun login(
        email: String,
        password: String
    ): Boolean
}