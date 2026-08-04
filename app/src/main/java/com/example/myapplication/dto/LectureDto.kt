package com.example.myapplication.dto

data class LectureListResponse(
    val content: List<LectureResponse>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val completedCount: Long
)

data class LectureResponse(
    val lectureId: Long,
    val title: String,
    val description: String,
    val category: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val createdAt: String,
    val isCompleted: Boolean
)