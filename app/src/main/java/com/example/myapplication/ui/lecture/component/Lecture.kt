package com.example.myapplication.ui.lecture.component

data class Lecture(
    val id:Long,
    val title:String,
    val description: String,
    val category: LectureCategory,
    val videoUrl:String,
    val thumbnailUrl:String,
    val isCompleted: Boolean
)