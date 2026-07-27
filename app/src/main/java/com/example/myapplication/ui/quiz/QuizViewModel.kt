package com.example.myapplication.ui.quiz

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
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // 서버에서 받아온 전체 퀴즈 목록과 현재 문제 인덱스
    private var quizzes: List<QuizUiState> = emptyList()
    private var currentIndex: Int = 0

    init {
        loadQuizzes()
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            runCatching {
                quizRepository.getQuizzes()
            }.onSuccess { list ->
                quizzes = list
                currentIndex = 0
                if (list.isNotEmpty()) {
                    _uiState.value = list[0]
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.onFailure {
                // 서버 미연결/통신 실패 시 앱이 죽지 않도록 처리
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectOption(option: String) {
        _uiState.update { it.copy(selectedOption = option) }
        // TODO: 답안 제출/채점(POST /quizzes/{quizId}/submit)은 userId 전달 방식 확정 후 연동
    }

    /** 다음 문제로 이동 (마지막이면 그대로 유지) */
    fun nextQuiz() {
        if (currentIndex < quizzes.lastIndex) {
            currentIndex++
            _uiState.value = quizzes[currentIndex]
        }
    }
}