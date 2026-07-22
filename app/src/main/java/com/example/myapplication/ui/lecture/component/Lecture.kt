package com.example.myapplication.ui.lecture.component

data class Lecture(
    val id:Long,
    val title:String,
    val description: String,
    val time:Int,
    val genre:String,
    val videoUrl:String,
    val thumbnailUrl:String
)