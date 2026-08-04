package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Lecture
import com.example.myapplication.ui.lecture.component.LectureCategory
import com.example.myapplication.ui.lecture.component.LectureListResult

interface LectureRepository {

    suspend fun getLectures(
        category: LectureCategory,
        page: Int=0,
        size: Int=10
    ): LectureListResult

    suspend fun getLectureById(
        lectureId: Long
    ): Lecture?
}