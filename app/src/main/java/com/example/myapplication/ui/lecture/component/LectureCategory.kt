package com.example.myapplication.ui.lecture.component

enum class LectureCategory (
    val displayName: String
){
    BASIC("기초 초성"),
    NUMBER("숫자"),
    EMOTION("감정"),
    GREETING("인사말"),
    LOCATION("나라명 및 지명"),
    CLOTHING("의생활"),
    FOOD("식생활"),
    HOUSING("주생활"),
    SOCIETY("사회생활"),
    ECONOMY("경제생활"),
    EDUCATION("교육"),
    RELIGION("종교"),
    ANIMAL_PLANT("동식물"),
    POLITICS("정치와 행정"),
    NATURE("자연"),
    CULTURE("문화"),
    LIFE("삶"),
    CONCEPT("개념"),
    HUMAN("인간"),
    DAILY("기타");

    companion object {
        fun fromApiValue(value: String): LectureCategory? {
            return entries.find {category ->
                category.name.equals(value, ignoreCase = true)
            }
        }
    }
}