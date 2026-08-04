package com.example.myapplication.ui.lecture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    private val lectureRepository: LectureRepository
): ViewModel() {

    var uiState by mutableStateOf(
        LectureDetailUiState()
    )
        private set

    private var loadedLectureId: Long? = null

    fun loadLecture(
        lectureId: Long
    ){
        if(
            loadedLectureId == lectureId &&
            uiState.lecture != null
        ) {
            return
        }

        loadedLectureId = lectureId

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                lectureRepository.getLectureById(lectureId)
            }.onSuccess { lecture ->
                uiState = uiState.copy(
                    lecture = lecture,
                    isLoading = false,
                    errorMessage = if(lecture == null) {
                        "강의를 찾을 수 없습니다."
                    } else {
                        null
                    }
                )
            }.onFailure {
                uiState=uiState.copy(
                    lecture = null,
                    isLoading = false,
                    errorMessage = "강의 정보를 불러오지 못했습니다."
                )
            }
        }
    }
}