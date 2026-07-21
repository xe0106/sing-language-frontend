package com.example.myapplication.ui.lecture

import com.example.myapplication.ui.lecture.component.Genre
import com.example.myapplication.ui.lecture.component.Lecture
import javax.inject.Inject

class LectureRepositoryImpl @Inject constructor(): LectureRepository{
    override suspend fun getGenres(): List<Genre> {
        return listOf(
            Genre("인간"),
            Genre("삶"),
            Genre("식생활"),
            Genre("의생활"),
            Genre("주생활"),
            Genre("사회생활"),
            Genre("경제생활"),
            Genre("교육"),
            Genre("나라명 및 지명"),
            Genre("종교"),
            Genre("문화"),
            Genre("정치와 행정"),
            Genre("자연"),
            Genre("동식물"),
            Genre("개념"),
            Genre("기타"),
        )
    }

    override suspend fun getLectures(): List<Lecture> {
        return listOf(
            Lecture(
                id = 1L,
                title = "기초 인사 수어",
                description = "안녕하세요, 반갑습니다 등의 기본 인사를 배워요.",
                time = 5,
                genre = "인간",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                thumbnailUrl = "https://picsum.photos/seed/lecture1/400/300"
            ),
            Lecture(
                id = 2L,
                title = "가족 표현 배우기",
                description = "엄마, 아빠, 형제 등 가족과 관련된 수어를 배워요.",
                time = 8,
                genre = "인간",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                thumbnailUrl = "https://picsum.photos/seed/lecture2/400/300"
            ),
            Lecture(
                id = 3L,
                title = "감정 표현 배우기",
                description = "기쁨, 슬픔, 화남 등의 감정을 수어로 표현해요.",
                time = 7,
                genre = "삶",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                thumbnailUrl = "https://picsum.photos/seed/lecture3/400/300"
            ),
            Lecture(
                id = 4L,
                title = "학교생활 수어",
                description = "학교에서 자주 사용하는 단어들을 배워요.",
                time = 10,
                genre = "삶",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                thumbnailUrl = "https://picsum.photos/seed/lecture4/400/300"
            ),
            Lecture(
                id = 5L,
                title = "일상 대화 수어",
                description = "일상생활에서 사용할 수 있는 짧은 문장을 배워요.",
                time = 12,
                genre = "식생활",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                thumbnailUrl = "https://picsum.photos/seed/lecture5/400/300"
            )
        )
    }

    override suspend fun getLectureById(id: Long): Lecture? {
        return getLectures().find{it.id==id}
    }
}