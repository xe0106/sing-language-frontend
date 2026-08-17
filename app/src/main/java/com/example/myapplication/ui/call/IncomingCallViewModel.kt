package com.example.myapplication.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.call.CallSocketConnectionState
import com.example.myapplication.ui.call.call_receive.IncomingCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    val incomingCalls: Flow<IncomingCall> =
        callRepository.incomingCallEvents

    val connectionState =
        callRepository.incomingCallSocketConnectionState

    private var connectionJob: Job? = null

    fun startListening() {
        val currentState = connectionState.value

        if (
            currentState ==
            CallSocketConnectionState.CONNECTING ||
            currentState ==
            CallSocketConnectionState.CONNECTED
        ) {
            return
        }

        connectionJob?.cancel()

        connectionJob = viewModelScope.launch {
            runCatching {
                callRepository.connectIncomingCallSocket()
            }
        }
    }

    fun stopListening() {
        connectionJob?.cancel()
        connectionJob = null

        viewModelScope.launch {
            runCatching {
                callRepository.disconnectIncomingCallSocket()
            }
        }
    }
}