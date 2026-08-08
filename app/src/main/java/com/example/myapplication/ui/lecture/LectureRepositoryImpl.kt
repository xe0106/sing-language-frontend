package com.example.myapplication.ui.lecture

import com.example.myapplication.api.LectureApiService
import com.example.myapplication.dto.LectureResponse
import com.example.myapplication.ui.lecture.component.Lecture
import com.example.myapplication.ui.lecture.component.LectureCategory
import com.example.myapplication.ui.lecture.component.LectureListResult
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class LectureRepositoryImpl @Inject constructor(
    private val lectureApiService: LectureApiService
): LectureRepository{

    private val lectureCache = ConcurrentHashMap<Long, Lecture>()

    override suspend fun getLectures(
        category: LectureCategory,
        page: Int,
        size: Int
    ): LectureListResult {
        val response = lectureApiService.viewLectureList(
            category = category.name,
            page=page,
            size = size
        )

        val body = response.body()

        if(
            !response.isSuccessful ||
            body?.isSuccess != true ||
            body.data == null
        ) {
            throw IllegalStateException(
                body?.message ?: "강의 목록 조회에 실패했습니다."
            )
        }

        val data = body.data

        val lectures = data.content.map { dto ->
            dto.toLecture()
        }

        lectures.forEach { lecture ->
            lectureCache[lecture.id] = lecture
        }

        return LectureListResult(
            lectures = lectures,
            pageNumber = data.pageNumber,
            pageSize = data.pageSize,
            totalElements = data.totalElements,
            totalPages = data.totalPages,
            completedCount = data.completedCount
        )
    }

    override suspend fun getLectureById(
        lectureId: Long
    ): Lecture? {
        lectureCache[lectureId]?.let { cachedLecture ->
            return cachedLecture
        }

        val response = lectureApiService.viewLectureDetail(
            lectureId = lectureId
        )

        val body = response.body()
        val data = body?.data

        if(
            !response.isSuccessful ||
            body?.isSuccess != true ||
            data == null
        ) {
            throw IllegalStateException(
                body?.message ?: "강의 상세 조회에 실패했습니다."
            )
        }

        return data.toLecture().also { lecture ->
            lectureCache[lecture.id] = lecture
        }
    }
}

private fun LectureResponse.toLecture(): Lecture {
    val lectureCategory =
        LectureCategory.fromApiValue(category)
            ?: throw IllegalStateException(
                "지원하지 않는 카테고리입니다: ${category}"
            )

    return Lecture(
        id=lectureId,
        title=title,
        description=description,
        category = lectureCategory,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        isCompleted = isCompleted
    )
}