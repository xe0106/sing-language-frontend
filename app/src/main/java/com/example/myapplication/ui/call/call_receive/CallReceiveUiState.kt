package com.example.myapplication.ui.call.call_receive

data class CallReceiveUiState(
    val incomingCall: IncomingCall?=null,
    val isLoading: Boolean = false,
    val isAccepting: Boolean = false,
    val isRejecting: Boolean = false,
    val errorMessage: String? = null,
    val isAcceptSuccess: Boolean = false,
    val isRejectSuccess: Boolean = false
)