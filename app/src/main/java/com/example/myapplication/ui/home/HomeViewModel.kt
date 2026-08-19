package com.example.myapplication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

    /** 퀴즈 완료 후 진도율을 다시 불러올 때 MainNavHost 에서 호출한다. */
    fun refresh() {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 홈 정보와 최근 연락처를 병렬로 조회한다
            val homeDeferred = async { homeRepository.getHome() }
            val contactsDeferred = async { homeRepository.getRecentContacts(limit = 3) }

            val homeState = homeDeferred.await()
            val contacts = contactsDeferred.await()

            _uiState.value = homeState.copy(
                recentContacts = contacts,
                isLoading = false
            )
        }
    }
}