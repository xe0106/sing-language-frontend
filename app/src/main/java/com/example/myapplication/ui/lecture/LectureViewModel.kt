package com.example.myapplication.ui.lecture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.lecture.component.LectureCategory
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
        loadLectures(
            category = LectureCategory.BASIC
        )
    }

    private fun loadLectures(
        category: LectureCategory = uiState.selectedCategory,
        page: Int = 0
    ){
        viewModelScope.launch{
            uiState=uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                lectureRepository.getLectures(
                    category = category,
                    page = page,
                    size = uiState.pageSize
                )
            }.onSuccess { result ->
                uiState=uiState.copy(
                    lectures = result.lectures,
                    selectedCategory = category,
                    pageNumber = result.pageNumber,
                    pageSize = result.pageSize,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                    completedCount = result.completedCount,
                    isLoading = false
                )
            }.onFailure {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "강의 목록을 불러오지 못했습니다."
                )
            }
        }
    }

    fun retryLectures() {
        loadLectures(
            category = uiState.selectedCategory,
            page = 0
        )
    }

    fun onCategoryClick(category: LectureCategory){
        uiState=uiState.copy(selectedCategory = category)

        loadLectures(
            category = category,
            page = 0
        )
    }

    fun toggleSearch() {
        uiState = if (uiState.isSearchVisible) {
            uiState.copy(
                searchQuery = "",
                isSearchVisible = false
            )
        } else {
            uiState.copy(isSearchVisible = true)
        }
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun clearErrorMessage(){
        uiState=uiState.copy(errorMessage = null)
    }
}
