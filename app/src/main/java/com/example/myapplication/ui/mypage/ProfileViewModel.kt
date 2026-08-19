package com.example.myapplication.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCheckingNickname: Boolean = false,
    val profile: ProfileDto? = null,
    val nickname: String = "",
    val originalNickname: String = "",
    val phoneNumber: String = "",
    val originalPhoneNumber: String = "",
    val gender: String = "",
    val originalGender: String = "",
    val birthDate: String = "",
    val originalBirthDate: String = "",
    val profileImageUrl: String? = null,
    val profileImageUri: String? = null,
    val isNicknameChecked: Boolean = false,
    val isUpdateSuccess: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** 프로필 수정 화면 진입 시 현재 서버 값을 입력 상태로 초기화한다. */
    fun loadProfile() {
        if (_uiState.value.isLoading || _uiState.value.profile != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            runCatching { profileRepository.getProfile() }
                .onSuccess { profile ->
                    if (profile == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "프로필 정보를 불러오지 못했습니다."
                            )
                        }
                        return@onSuccess
                    }

                    val nickname = profile.nickname.orEmpty()
                    val phoneNumber = profile.phoneNumber.orEmpty()
                    val gender = profile.gender.normalizeGender()
                    val birthDate = profile.birthDate.orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            nickname = nickname,
                            originalNickname = nickname,
                            phoneNumber = phoneNumber,
                            originalPhoneNumber = phoneNumber,
                            gender = gender,
                            originalGender = gender,
                            birthDate = birthDate,
                            originalBirthDate = birthDate,
                            profileImageUrl = profile.profileImageUrl,
                            isNicknameChecked = nickname.isNotBlank()
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "프로필 정보를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun onNicknameChange(nickname: String) {
        _uiState.update {
            it.copy(
                nickname = nickname,
                isNicknameChecked = nickname.trim() == it.originalNickname.trim(),
                message = null
            )
        }
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        _uiState.update { it.copy(phoneNumber = phoneNumber, message = null) }
    }

    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(gender = gender, message = null) }
    }

    fun onBirthDateChange(birthDate: String) {
        _uiState.update { it.copy(birthDate = birthDate, message = null) }
    }

    fun onProfileImageChange(profileImageUri: String?) {
        if (profileImageUri == null) return
        _uiState.update { it.copy(profileImageUri = profileImageUri, message = null) }
    }

    fun checkNickname() {
        val state = _uiState.value
        val nickname = state.nickname.trim()

        if (nickname.isBlank()) {
            _uiState.update {
                it.copy(
                    isNicknameChecked = false,
                    message = "닉네임을 입력해 주세요."
                )
            }
            return
        }

        if (nickname == state.originalNickname.trim()) {
            _uiState.update {
                it.copy(
                    isNicknameChecked = true,
                    message = "현재 사용 중인 닉네임입니다."
                )
            }
            return
        }

        if (state.isCheckingNickname || state.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingNickname = true,
                    isNicknameChecked = false,
                    message = null
                )
            }

            val outcome = profileRepository.checkNickname(nickname)
            val nicknameIsStillCurrent = _uiState.value.nickname.trim() == nickname

            _uiState.update {
                it.copy(
                    isCheckingNickname = false,
                    isNicknameChecked = nicknameIsStillCurrent && outcome.isAvailable,
                    message = if (nicknameIsStillCurrent) outcome.message else null
                )
            }
        }
    }

    fun updateProfile() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.isCheckingNickname) return

        val nickname = state.nickname.trim()
        val phoneNumber = state.phoneNumber.trim()
        val gender = state.gender.trim()
        val birthDate = state.birthDate.trim()
        val validationMessage = when {
            nickname.isBlank() -> "닉네임을 입력해 주세요."
            gender.isBlank() -> "성별을 선택해 주세요."
            birthDate.isBlank() -> "생년월일을 입력해 주세요."
            !birthDate.isValidDate() -> "생년월일을 YYYY-MM-DD 형식으로 입력해 주세요."
            phoneNumber.isBlank() -> "전화번호를 입력해 주세요."
            nickname != state.originalNickname.trim() && !state.isNicknameChecked ->
                "닉네임 중복확인을 해주세요."

            else -> null
        }

        if (validationMessage != null) {
            _uiState.update { it.copy(message = validationMessage) }
            return
        }

        val nicknameToUpdate = nickname.takeIf {
            it != state.originalNickname.trim()
        }
        val phoneNumberToUpdate = phoneNumber.takeIf {
            it != state.originalPhoneNumber.trim()
        }
        val genderToUpdate = gender.takeIf {
            it != state.originalGender.trim()
        }
        val birthDateToUpdate = birthDate.takeIf {
            it != state.originalBirthDate.trim()
        }
        val imageUriToUpload = state.profileImageUri

        if (
            nicknameToUpdate == null &&
            genderToUpdate == null &&
            birthDateToUpdate == null &&
            phoneNumberToUpdate == null &&
            imageUriToUpload == null
        ) {
            _uiState.update { it.copy(message = "변경된 내용이 없습니다.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    isUpdateSuccess = false,
                    message = null
                )
            }

            val uploadedImageUrl = if (imageUriToUpload != null) {
                profileRepository.uploadProfileImage(imageUriToUpload)
                    ?: run {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                message = "프로필 이미지 업로드에 실패했습니다."
                            )
                        }
                        return@launch
                    }
            } else {
                null
            }

            val outcome = profileRepository.updateProfile(
                nickname = nicknameToUpdate,
                gender = genderToUpdate,
                birthDate = birthDateToUpdate,
                phoneNumber = phoneNumberToUpdate,
                profileImageUrl = uploadedImageUrl
            )

            _uiState.update {
                it.copy(
                    isSaving = false,
                    isUpdateSuccess = outcome.isSuccess,
                    message = if (outcome.isSuccess) null else outcome.message
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun consumeUpdateSuccess() {
        _uiState.update { it.copy(isUpdateSuccess = false) }
    }

    private fun String?.normalizeGender(): String = when (this?.uppercase()) {
        "MALE", "M", "남성" -> "MALE"
        "FEMALE", "F", "여성" -> "FEMALE"
        else -> this.orEmpty().uppercase()
    }

    private fun String.isValidDate(): Boolean =
        runCatching { LocalDate.parse(this) }.isSuccess
}
