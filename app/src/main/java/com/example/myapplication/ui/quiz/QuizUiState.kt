package com.example.myapplication.ui.quiz

data class QuizUiState(
    val quizId: Long? = null,
    val question: String = "",
    val imageRes: Int? = null,       // 더미(로컬 리소스)용 - 서버 연동 후에는 image3dUrl 사용
    val image3dUrl: String? = null,  // 서버에서 내려주는 3D 수어 모델(.glb) URL
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int? = null,
    val progress: Float = 0f,
    val selectedOption: String? = null,
    val isLoading: Boolean = true
)