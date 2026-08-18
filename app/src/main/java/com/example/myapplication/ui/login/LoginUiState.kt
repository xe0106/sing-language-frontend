package com.example.myapplication.ui.login

data class LoginUiState(
    val email: String="",
    val password: String="",
    val isLoading: Boolean=false
)
