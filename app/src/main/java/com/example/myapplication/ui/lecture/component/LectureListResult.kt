package com.example.myapplication.ui.lecture.component

data class LectureListResult(
    val lectures: List<Lecture>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val completedCount: Long
)