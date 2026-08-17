package com.example.myapplication.ui.call.call_home

data class CallUiState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingContact: Boolean = false,
    val isCalling: Boolean = false,
    val errorMessage: String? = null
)