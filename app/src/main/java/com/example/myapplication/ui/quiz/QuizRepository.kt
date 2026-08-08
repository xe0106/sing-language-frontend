package com.example.myapplication.ui.quiz

interface QuizRepository {

    /** 퀴즈 목록 조회 (기본 5문제) */
    suspend fun getQuizzes(count: Int = 5): Result<List<QuizItem>>

    /**
     * 답안 제출. 성공 여부만 돌려준다.
     * UI 채점은 correctOptionIndex 로 로컬 판정하므로 이 호출이 실패해도 화면은 정상 동작한다.
     */
    suspend fun submitQuiz(quizId: Long, selectedIndex: Int): Boolean
}