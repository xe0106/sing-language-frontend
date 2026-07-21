package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Genre
import com.example.myapplication.ui.lecture.component.Lecture

interface LectureRepository {
    suspend fun getGenres(): List<Genre>

    suspend fun getLectures():List<Lecture>

    suspend fun getLectureById(id: Long): Lecture?
}