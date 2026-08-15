package com.example.myapplication.ui.call.call_receive

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
class CallReceiveViewModel @Inject constructor(
    private val callRepository: CallRepository
): ViewModel(){
    var uiState by mutableStateOf(CallReceiveUiState())
        private set

    fun loadIncomingCall(callId: String){
        viewModelScope.launch {
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                callRepository.getIncomingCall(callId)
            }.onSuccess { incomingCall ->
                uiState=uiState.copy(
                    incomingCall=incomingCall,
                    isLoading = false
                )
            }.onFailure {
                uiState=uiState.copy(
                    isLoading = false,
                    errorMessage = "수신 정보를 불러오지 못했습니다."
                )
            }
        }
    }

    fun acceptCall(){
        val callId=uiState.incomingCall?.callId ?: return

        viewModelScope.launch {
            uiState=uiState.copy(
                isAccepting = true
            )

            runCatching {
                callRepository.acceptCall(callId)
            }.onSuccess {
                uiState=uiState.copy(
                    isAccepting = false,
                    isAcceptSuccess = true
                )
            }.onFailure {
                uiState=uiState.copy(
                    isAccepting = false,
                    errorMessage = "통화를 수락하지 못했습니다."
                )
            }
        }
    }

    fun rejectCall(){
        val callId=uiState.incomingCall?.callId ?: return

        viewModelScope.launch {
            uiState=uiState.copy(
                isRejecting = true
            )

            runCatching {
                callRepository.rejectCall(callId)
            }.onSuccess {
                uiState=uiState.copy(
                    isRejecting = false,
                    isRejectSuccess = true
                )
            }.onFailure {
                uiState=uiState.copy(
                    isRejecting = false,
                    errorMessage = "통화를 거절하지 못했습니다."
                )
            }
        }
    }
}