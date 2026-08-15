package com.example.myapplication.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPageUiState(
    val isLoading: Boolean = false,
    val profile: ProfileDto? = null,
    val errorMessage: String? = null
)

sealed interface MyPageEvent {
    data object NavigateToLogin : MyPageEvent
}

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MyPageEvent>()
    val event: SharedFlow<MyPageEvent> = _event.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.getProfile() }
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    /** 로그아웃. 서버 실패와 무관하게 로그인 화면으로 이동한다. */
    fun logout() {
        viewModelScope.launch {
            runCatching { repository.logout() }
            _event.emit(MyPageEvent.NavigateToLogin)
        }
    }

    /** 회원 탈퇴. 성공했을 때만 로그인 화면으로 이동한다. */
    fun withdraw() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.withdraw() }
                .onSuccess { success ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (success) null else "탈퇴에 실패했습니다."
                        )
                    }
                    if (success) _event.emit(MyPageEvent.NavigateToLogin)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}