package com.example.myapplication.dto

/**
 * 홈 화면 정보 응답 (GET /sign/language/home)
 *
 * 실제 응답:
 * {
 *   "currentDate": "08/08 (토)",
 *   "greetingMessage": "즐거운 오후예요, 오혁님!",
 *   "goalTitle": "수어 단어 5개 익히기",
 *   "progressPercentage": 80
 * }
 *
 * - 날짜/인사말 문구는 서버가 완성해서 내려준다. 앱에서 조립하지 않는다.
 * - progressPercentage 는 0~100 정수. Compose 진행바는 0f~1f 이므로 변환 필요.
 * - userId 는 응답에 없다. 서버가 토큰으로 사용자를 식별한다.
 */
data class HomeResponse(
    val currentDate: String?,
    val greetingMessage: String?,
    val goalTitle: String?,
    val progressPercentage: Int?
)