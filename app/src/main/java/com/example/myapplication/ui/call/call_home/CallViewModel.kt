package com.example.myapplication.ui.call.call_home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.call.CallRepository
import com.example.myapplication.ui.mypage.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val deviceContactReader: DeviceContactReader,
    private val profileRepository: ProfileRepository
): ViewModel(){

    sealed interface CallEvent {
        data class NavigateToVideoCall(
            val callId: String
        ): CallEvent
    }

    private val _event = MutableSharedFlow<CallEvent>()
    val event: SharedFlow<CallEvent> = _event.asSharedFlow()

    var uiState by mutableStateOf(CallUiState())
        private set

    fun startCall(
        contact: Contact
    ) {
        if(uiState.isCalling) return

        viewModelScope.launch {
            uiState = uiState.copy(
                isCalling = true,
                errorMessage = null
            )

            runCatching {
                callRepository.startCall(
                    receiverId = contact.targetUserId
                )
            }.onSuccess { session ->
                uiState = uiState.copy(
                    isCalling = false
                )

                _event.emit(
                    CallEvent.NavigateToVideoCall(
                        callId = session.callId
                    )
                )
            }.onFailure { exception ->
                uiState = uiState.copy(
                    isCalling = false,
                    errorMessage = exception.message ?: "전화를 발신하지 못했습니다."
                )
            }
        }
    }

    init {
        loadContacts()
        loadLearningDays()
    }

    private fun loadLearningDays() {
        viewModelScope.launch {
            runCatching {
                profileRepository.getProfile()?.learningDays ?: 0
            }.onSuccess { learningDays ->
                uiState = uiState.copy(
                    learningDays = learningDays
                )
            }
        }
    }

    fun loadContacts(){
        viewModelScope.launch {
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                callRepository.getContacts()
            }.onSuccess { contacts->
                uiState=uiState.copy(
                    contacts=contacts,
                    isLoading = false
                )
            }.onFailure {
                uiState=uiState.copy(
                    isLoading = false,
                    errorMessage = "연락처 목록을 불러오지 못했습니다."
                )
            }
        }
    }

    fun addContactFromDevice(contactUri: Uri){
        viewModelScope.launch{
            uiState=uiState.copy(
                isAddingContact = true,
                errorMessage = null
            )

            runCatching {
                val contact=deviceContactReader.readContact(contactUri)
                callRepository.addContact(contact)
                callRepository.getContacts()
            }.onSuccess { contacts ->
                uiState=uiState.copy(
                    contacts=contacts,
                    isAddingContact = false
                )
            }.onFailure {
                uiState=uiState.copy(
                    isAddingContact = false,
                    errorMessage = "연락처를 추가하지 못했습니다."
                )
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            runCatching {
                callRepository.deleteContact(
                    targetUserId = contact.targetUserId
                )
            }.onSuccess {
                uiState = uiState.copy(
                    contacts = uiState.contacts.filterNot {
                        it.contactId == contact.contactId
                    }
                )
            }.onFailure {
                uiState = uiState.copy(
                    errorMessage = "연락처를 삭제하지 못했습니다."
                )
            }
        }
    }
}
