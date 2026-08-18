package com.example.myapplication.ui.call.call_home

data class CallUiState(
    val contacts: List<Contact> = emptyList(),
    val learningDays: Int = 0,
    val isLoading: Boolean = false,
    val isAddingContact: Boolean = false,
    val isCalling: Boolean = false,
    val errorMessage: String? = null
)
