package com.example.myapplication.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
): ViewModel(){
    sealed interface LoginEvent {
        data class LoginResult(
            val isSuccess: Boolean,
            val message: String
        ) : LoginEvent
    }

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event.asSharedFlow()

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
        if (uiState.isLoading) return

        val validationMessage = when {
            uiState.email.isBlank() ->
                "이메일은 필수 입력값입니다."

            uiState.password.isBlank() ->
                "비밀번호는 필수 입력값입니다."

            else -> null
        }

        if (validationMessage != null) {
            viewModelScope.launch {
                _event.emit(
                    LoginEvent.LoginResult(
                        isSuccess = false,
                        message = validationMessage
                    )
                )
            }
            return
        }

        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true
            )

            val outcome = loginRepository.login(
                email=uiState.email,
                password = uiState.password
            )

            uiState=uiState.copy(
                isLoading = false
            )

            _event.emit(
                LoginEvent.LoginResult(
                    isSuccess = outcome.isSuccess,
                    message = outcome.message
                )
            )
        }
    }
}
