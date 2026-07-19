package com.example.myapplication.ui.register

data class RegisterUiState(
    val profileImageUri: String? = null,
    val gender: Gender= Gender.MALE,
    val nickname: String = "",
    val birth: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNicknameSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false
)