package com.example.myapplication.ui.lecture

data class Lecture(
    val id:Long,
    val title:String,
    val description: String,
    val time:Int,
    val videoUrl:String,
    val thumbnailUrl:String
)
