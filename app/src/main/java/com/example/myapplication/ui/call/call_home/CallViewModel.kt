package com.example.myapplication.ui.call.call_home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.call.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val deviceContactReader: DeviceContactReader
): ViewModel(){

    var uiState by mutableStateOf(CallUiState())
        private set

    init {
        loadContacts()
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