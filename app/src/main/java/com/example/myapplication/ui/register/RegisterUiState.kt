package com.example.myapplication.ui.register

data class RegisterUiState(
    // 이미지 미리보기와 파일 읽기용
    val profileImageUri: String?= null,

    // 이미지 업로드 성공 후 서버가 반환한 값
    val profileImageUrl: String? = null,

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