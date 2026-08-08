package com.example.myapplication.ui.home

data class HomeUiState(
    /** 서버가 완성해서 주는 날짜 문자열. 예: "08/08 (토)" */
    val currentDate: String = "",
    /** 서버가 완성해서 주는 인사말. 예: "즐거운 오후예요, 오혁님!" */
    val greetingMessage: String = "",
    /** 오늘의 목표. 예: "수어 단어 5개 익히기" */
    val goalTitle: String = "",
    /** 0f ~ 1f (서버는 0~100 정수로 주므로 Repository 에서 변환) */
    val progress: Float = 0f,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)