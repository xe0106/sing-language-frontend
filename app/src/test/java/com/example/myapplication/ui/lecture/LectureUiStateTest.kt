package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Lecture
import com.example.myapplication.ui.lecture.component.LectureCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class LectureUiStateTest {

    @Test
    fun `검색어가 비어 있으면 모든 강의를 표시한다`() {
        val lectures = lectures()
        val state = LectureUiState(
            lectures = lectures,
            searchQuery = "   "
        )

        assertEquals(lectures, state.filteredLectures)
    }

    @Test
    fun `제목에 검색어가 포함된 강의만 표시한다`() {
        val state = LectureUiState(
            lectures = lectures(),
            searchQuery = "인사"
        )

        assertEquals(
            listOf(1L, 3L),
            state.filteredLectures.map { it.id }
        )
    }

    @Test
    fun `영문 검색은 대소문자를 구분하지 않는다`() {
        val state = LectureUiState(
            lectures = lectures(),
            searchQuery = "BASIC"
        )

        assertEquals(
            listOf(2L),
            state.filteredLectures.map { it.id }
        )
    }

    private fun lectures(): List<Lecture> = listOf(
        lecture(id = 1L, title = "기초 인사 배우기"),
        lecture(id = 2L, title = "Basic 숫자 표현"),
        lecture(id = 3L, title = "일상 인사 표현")
    )

    private fun lecture(
        id: Long,
        title: String
    ): Lecture = Lecture(
        id = id,
        title = title,
        description = "",
        category = LectureCategory.BASIC,
        videoUrl = "",
        thumbnailUrl = "",
        isCompleted = false
    )
}
