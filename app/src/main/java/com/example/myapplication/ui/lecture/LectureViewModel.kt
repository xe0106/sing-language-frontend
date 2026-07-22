package com.example.myapplication.ui.lecture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.lecture.component.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureViewModel @Inject constructor(
    private val lectureRepository: LectureRepository
): ViewModel(){

    var uiState by mutableStateOf(LectureUiState())
        private set

    init {
        loadLectures()
    }

    fun loadLectures(){
        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val genres=lectureRepository.getGenres()
                val lectures=lectureRepository.getLectures()

                uiState=uiState.copy(
                    genres=genres,
                    selectedGenre = genres.firstOrNull(),
                    lectures=lectures,
                    isLoading = false
                )
            }.onFailure {
                uiState=uiState.copy(
                    isLoading = false,
                    errorMessage = "강의 목록을 불러오지 못했습니다."
                )
            }
        }
    }

    fun onGenreClick(genre: Genre){
        uiState=uiState.copy(selectedGenre = genre)
    }

    fun loadLectureDetail(lectureId:Long){
        viewModelScope.launch{
            uiState=uiState.copy(
                selectedLecture = null,
                isDetailLoading = true,
                detailErrorMessage = null
            )

            runCatching {
                lectureRepository.getLectureById(lectureId)
            }.onSuccess { lecture->
                uiState=uiState.copy(
                    selectedLecture = lecture,
                    isDetailLoading = false,
                    detailErrorMessage = if(lecture==null){
                        "강의를 찾을 수 없습니다."
                    } else{
                        null
                    }
                )
            }.onFailure {
                uiState=uiState.copy(
                    selectedLecture = null,
                    isDetailLoading = false,
                    detailErrorMessage = "강의 정보를 불러오지 못했습니다."
                )
            }
        }
    }

    fun clearErrorMessage(){
        uiState=uiState.copy(errorMessage = null)
    }
}