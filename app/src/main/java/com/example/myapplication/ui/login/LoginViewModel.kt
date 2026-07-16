package com.example.myapplication.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
): ViewModel(){
    var uiState by mutableStateOf(LoginUiState())
        private set

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

    fun login(){
        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            val isSuccess=loginRepository.login(
                email=uiState.email,
                password = uiState.password
            )

            uiState=uiState.copy(
                isLoading = false,
                isLoginSuccess = isSuccess,
                errorMessage = if(isSuccess) null else "로그인에 실패했습니다."
            )
        }
    }
}