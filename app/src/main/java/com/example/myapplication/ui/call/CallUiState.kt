package com.example.myapplication.ui.call

data class CallUiState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingContact: Boolean = false,
    val errorMessage: String? = null
)