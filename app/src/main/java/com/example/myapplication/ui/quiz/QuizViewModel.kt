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

    init {
        loadQuizzes()
    }

    fun loadQuizzes(count: Int = 5) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            quizRepository.getQuizzes(count)
                .onSuccess { list ->
                    _uiState.value = QuizUiState(
                        quizzes = list,
                        isLoading = false
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "퀴즈를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    /** 보기 선택 → 즉시 채점(로컬) + 서버에 제출 */
    fun selectOption(index: Int) {
        val state = _uiState.value
        // 이미 정답을 공개한 뒤에는 다시 못 고르게 한다
        if (state.isAnswerRevealed) return
        val quiz = state.currentQuiz ?: return

        val isCorrect = index == quiz.correctOptionIndex

        _uiState.update {
            it.copy(
                selectedIndex = index,
                isAnswerRevealed = true,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount
            )
        }

        // 서버 제출은 실패해도 화면에 영향 없음 (로컬 채점이 이미 끝났으므로)
        viewModelScope.launch {
            quizRepository.submitQuiz(quizId = quiz.quizId, selectedIndex = index)
        }
    }

    /** 다음 문제로. 마지막이면 종료 상태로 전환 */
    fun nextQuiz() {
        val state = _uiState.value
        if (state.isLastQuiz) {
            _uiState.update { it.copy(isFinished = true) }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedIndex = null,
                    isAnswerRevealed = false
                )
            }
        }
    }

    /** 처음부터 다시 (새 문제 세트를 받아온다) */
    fun restart() {
        loadQuizzes()
    }
}