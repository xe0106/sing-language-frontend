package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Lecture
import com.example.myapplication.ui.lecture.component.LectureCategory

data class LectureUiState(
    val categories: List<LectureCategory> =
        LectureCategory.entries.toList(),
    val selectedCategory: LectureCategory =
        LectureCategory.BASIC,

    val lectures: List<Lecture> = emptyList(),
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,

    val pageNumber: Int = 0,
    val pageSize: Int = 50,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val completedCount: Long = 0,

    val isLoading : Boolean=false,
    val errorMessage: String? =null,
) {
    val filteredLectures: List<Lecture>
        get() {
            val query = searchQuery.trim()

            return if (query.isEmpty()) {
                lectures
            } else {
                lectures.filter { lecture ->
                    lecture.title.contains(
                        other = query,
                        ignoreCase = true
                    )
                }
            }
        }
}
