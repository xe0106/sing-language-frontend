package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Genre
import com.example.myapplication.ui.lecture.component.Lecture

data class LectureUiState(
    val genres: List<Genre> = emptyList(),
    val selectedGenre: Genre?=null,
    val lectures: List<Lecture> = emptyList(),
    val selectedLecture: Lecture?=null,
    val isLoading : Boolean=false,
    val isDetailLoading: Boolean=false,
    val errorMessage: String? =null,
    val detailErrorMessage: String? =null
){
    val filteredLectures: List<Lecture>
        get() = if (selectedGenre==null){
            lectures
        } else {
            lectures.filter {it.genre ==selectedGenre.name}
        }
}