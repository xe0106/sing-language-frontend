package com.example.myapplication.ui.register

import android.R.attr.name
import android.R.attr.password
import android.R.attr.phoneNumber
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.login.LoginUiState
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
        uiState = uiState.copy(profileImageUri = profileImageUri)
    }

    fun onNicknameChange(
        nickname:String
    ){
        uiState=uiState.copy(
            nickname=nickname
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
                errorMessage = if(isSuccess) null else "중복된 닉네임입니다."
            )
        }
    }

    fun register(){
        if (uiState.password != uiState.passwordConfirm) {
            uiState = uiState.copy(
                errorMessage = "비밀번호가 일치하지 않습니다."
            )
            return
        }

        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            val isSuccess=registerRepository.register(
                name=uiState.name,
                email = uiState.email,
                password = uiState.password
            )

            uiState=uiState.copy(
                isLoading = false,
                isRegisterSuccess = isSuccess,
                errorMessage = if(isSuccess) null else "회원가입에 실패했습니다."
            )
        }
    }

    fun clearErrorMessage() {
        uiState = uiState.copy(errorMessage = null)
    }
}