package com.example.myapplication.ui.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.SessionManager
import com.example.myapplication.network.call.CallSocketConnectionState
import com.example.myapplication.ui.call.call_receive.IncomingCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val incomingCalls: Flow<IncomingCall> =
        callRepository.incomingCallEvents

    val connectionState =
        callRepository.incomingCallSocketConnectionState

    private var listeningRequested = false
    private var supervisorJob: Job? = null
    private var stopJob: Job? = null

    fun startListening() {
        listeningRequested = true

        if (supervisorJob?.isActive == true) return

        supervisorJob = viewModelScope.launch {
            stopJob?.join()

            var retryDelayMillis = INITIAL_RETRY_DELAY_MILLIS

            while (isActive && listeningRequested) {
                when (connectionState.value) {
                    CallSocketConnectionState.CONNECTED -> {
                        retryDelayMillis = INITIAL_RETRY_DELAY_MILLIS
                        connectionState.first { state ->
                            state != CallSocketConnectionState.CONNECTED
                        }
                    }

                    CallSocketConnectionState.CONNECTING -> {
                        connectionState.first { state ->
                            state != CallSocketConnectionState.CONNECTING
                        }
                    }

                    CallSocketConnectionState.DISCONNECTED,
                    CallSocketConnectionState.FAILED -> {
                        val result = runCatching {
                            callRepository.connectIncomingCallSocket()
                        }

                        if (result.isSuccess) {
                            retryDelayMillis = INITIAL_RETRY_DELAY_MILLIS
                        } else {
                            Log.e(
                                LOG_TAG,
                                "Failed to connect incoming-call socket; retry in ${retryDelayMillis}ms",
                                result.exceptionOrNull()
                            )

                            delay(retryDelayMillis)
                            retryDelayMillis =
                                (retryDelayMillis * 2)
                                    .coerceAtMost(MAX_RETRY_DELAY_MILLIS)
                        }
                    }
                }
            }
        }
    }

    fun ensureListening() {
        val hasSession =
            sessionManager.userId != null &&
                    !sessionManager.accessToken.isNullOrBlank()

        if (hasSession) {
            startListening()
        }
    }

    fun stopListening() {
        listeningRequested = false

        supervisorJob?.cancel()
        supervisorJob = null

        stopJob = viewModelScope.launch {
            runCatching {
                callRepository.disconnectIncomingCallSocket()
            }.onFailure { exception ->
                Log.e(
                    LOG_TAG,
                    "Failed to disconnect incoming-call socket",
                    exception
                )
            }
        }
    }

    private companion object {
        const val LOG_TAG = "IncomingCallSocket"
        const val INITIAL_RETRY_DELAY_MILLIS = 1_000L
        const val MAX_RETRY_DELAY_MILLIS = 30_000L
    }
}
