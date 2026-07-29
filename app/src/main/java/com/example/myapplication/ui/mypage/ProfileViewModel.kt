package com.example.myapplication.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileDto? = null,
    val isUpdateSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** 프로필 조회 (마이페이지/프로필 수정 화면 진입 시 호출) */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                profileRepository.getProfile()
            }.onSuccess { profile ->
                _uiState.update { it.copy(isLoading = false, profile = profile) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /**
     * 프로필 수정
     * - ProfileEditScreen 의 저장 버튼에 연결해서 사용
     * - 비어 있는 값은 null 로 보내 서버가 기존 값을 유지하도록 함
     * - 명세서상 수정 가능 필드: 닉네임/전화번호/프로필이미지/알림설정
     */
    fun updateProfile(
        nickname: String,
        phone: String,
        profileImageUrl: String? = null,
        notificationEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                profileRepository.updateProfile(
                    nickname = nickname.ifBlank { null },
                    phoneNumber = phone.ifBlank { null },
                    profileImageUrl = profileImageUrl,
                    notificationEnabled = notificationEnabled
                )
            }.onSuccess { success ->
                _uiState.update { it.copy(isLoading = false, isUpdateSuccess = success) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // TODO: 회원 탈퇴 API는 명세서에 아직 없음. 명세 나오면 추가.
}