package com.example.myapplication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeInfo()
    }

    private fun loadHomeInfo() {
        viewModelScope.launch {
            runCatching {
                homeRepository.getHomeInfo()
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure {
                // 서버 미연결/통신 실패 시 앱이 죽지 않도록 처리
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}