package com.example.myapplication.ui.login

interface LoginRepository {
    suspend fun login(
        email: String,
        password: String
    ): LoginOutcome
}

data class LoginOutcome(
    val isSuccess: Boolean,
    val message: String
)
