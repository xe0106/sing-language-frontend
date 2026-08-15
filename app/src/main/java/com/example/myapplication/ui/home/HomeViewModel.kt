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
        loadHome()
    }

    /**
     * 퀴즈 완료 후 진도율을 다시 불러올 때 MainNavHost 에서 호출한다.
     */
    fun refresh() {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { homeRepository.getHome() }
                .onSuccess { newState ->
                    _uiState.value = newState.copy(isLoading = false, errorMessage = null)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "홈 정보를 불러오지 못했습니다.")
                    }
                }
        }
    }
}