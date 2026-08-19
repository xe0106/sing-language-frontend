package com.example.myapplication.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerRepository: RegisterRepository
): ViewModel(){
    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onProfileImageChange(
        profileImageUri: String?
    ) {
        uiState = uiState.copy(
            profileImageUri = profileImageUri,
            profileImageUrl = null
        )
    }

    fun onNicknameChange(
        nickname:String
    ){
        uiState=uiState.copy(
            nickname=nickname,
            isNicknameSuccess = false,
            errorMessage = null
        )
    }

    fun onGenderChange(
        gender: Gender
    ) {
        uiState = uiState.copy(gender = gender)
    }

    fun onBirthChange(
        birth:String
    ){
        uiState=uiState.copy(
            birth = birth
        )
    }

    fun onPhoneNumberChange(
        phoneNumber:String
    ){
        uiState=uiState.copy(
            phoneNumber=phoneNumber
        )
    }

    fun onNameChange(
        name:String
    ){
        uiState=uiState.copy(
            name=name
        )
    }

    fun onEmailChange(
        email:String
    ){
        uiState=uiState.copy(
            email=email
        )
    }

    fun onPasswordChange(
        password:String
    ){
        uiState=uiState.copy(
            password=password
        )
    }
    fun onPasswordConfirmChange(
        passwordConfirm:String
    ){
        uiState=uiState.copy(
            passwordConfirm=passwordConfirm
        )
    }

    fun nicknameCheck(){
        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            val isSuccess=registerRepository.nicknameCheck(
                nickname=uiState.nickname
            )

            uiState=uiState.copy(
                isLoading = false,
                isNicknameSuccess = isSuccess,
                errorMessage = if(isSuccess) "사용 가능한 닉네임입니다." else "중복된 닉네임입니다."
            )
        }
    }

    fun register(){
        // 중복 요청 방지
        if (uiState.isLoading) {
            return
        }

        val validationMessage = when {
            uiState.email.isBlank() -> "이메일은 필수 입력값입니다."
            uiState.password.isBlank() -> "비밀번호는 필수 입력값입니다."
            uiState.name.isBlank() -> "이름은 필수 입력값입니다."
            uiState.nickname.isBlank() -> "닉네임은 필수 입력값입니다."
            uiState.birth.isBlank() -> "생년월일을 입력해주세요."
            uiState.phoneNumber.isBlank() -> "전화번호는 필수 입력값입니다."
            uiState.passwordConfirm.isBlank() -> "비밀번호 확인을 입력해 주세요."
            else -> null
        }

        if (validationMessage != null) {
            uiState = uiState.copy(errorMessage = validationMessage)
            return
        }

        if(!uiState.isNicknameSuccess) {
            uiState=uiState.copy(
                errorMessage = "닉네임 중복 확인을 해주세요."
            )
            return
        }

        if (uiState.password != uiState.passwordConfirm) {
            uiState = uiState.copy(
                errorMessage = "비밀번호가 일치하지 않습니다."
            )
            return
        }

        // 선택한 이미지의 안드로이드 로컬 URI
        val profileImageUri = uiState.profileImageUri

        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )
            // 이미 업로드된 URL이 있으면 재사용하고,
            // 없으면 이미지 업로드 API를 호출
            val profileImageUrl = uiState.profileImageUrl
                ?: registerRepository.uploadProfileImage(
                    profileImageUri = profileImageUri
                )

            if (profileImageUrl == null) {
                uiState=uiState.copy(
                    isLoading = false,
                    errorMessage = "이미지 업로드에 실패했습니다."
                )
                return@launch
            }

            // 서버가 반환한 URL을 상태에 저장
            uiState = uiState.copy(
                profileImageUrl = profileImageUrl
            )

            val outcome=registerRepository.register(
                email = uiState.email.trim(),
                password = uiState.password,
                name = uiState.name.trim(),
                profileImageUrl = profileImageUrl,
                nickname = uiState.nickname.trim(),
                gender = uiState.gender.name,
                birthDate = uiState.birth.trim(),
                phoneNumber = uiState.phoneNumber.trim()
            )

            uiState=uiState.copy(
                isLoading = false,
                isRegisterSuccess = outcome.isSuccess,
                errorMessage =
                    if(outcome.isSuccess) null
                    else outcome.message ?: "회원가입에 실패했습니다."
            )
        }
    }

    fun clearErrorMessage() {
        uiState = uiState.copy(errorMessage = null)
    }
}
