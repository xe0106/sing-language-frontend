package com.example.myapplication.ui.quiz

/** 퀴즈 한 문제 */
data class QuizItem(
    val quizId: Long,
    val question: String,
    /** 수어 이미지 URL (서버 필드명은 image3dUrl 이지만 실제로는 JPG) */
    val imageUrl: String?,
    val options: List<String>,
    /** 0-based 로 변환된 정답 인덱스. 서버는 1-based 로 주므로 Repository 에서 -1 처리 */
    val correctOptionIndex: Int?
)

data class QuizUiState(
    val quizzes: List<QuizItem> = emptyList(),
    val currentIndex: Int = 0,
    /** 사용자가 고른 보기 인덱스 (0-based). 아직 안 골랐으면 null */
    val selectedIndex: Int? = null,
    /** 정답 확인 후에만 true. 보기 색칠 여부를 결정한다 */
    val isAnswerRevealed: Boolean = false,
    val correctCount: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val currentQuiz: QuizItem?
        get() = quizzes.getOrNull(currentIndex)

    val totalCount: Int
        get() = quizzes.size

    /** 진행률 0f~1f */
    val progress: Float
        get() = if (quizzes.isEmpty()) 0f else (currentIndex + 1).toFloat() / quizzes.size

    val isLastQuiz: Boolean
        get() = quizzes.isNotEmpty() && currentIndex == quizzes.lastIndex

    /** 현재 문제를 맞췄는지 (정답 공개 상태일 때만 의미 있음) */
    val isCurrentCorrect: Boolean
        get() = selectedIndex != null && selectedIndex == currentQuiz?.correctOptionIndex
}