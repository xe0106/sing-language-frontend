package com.example.myapplication.dto

/**
 * 퀴즈 문제 (GET /sign/language/quizzes?count=5 의 data 배열 요소)
 *
 * 실제 응답:
 * {
 *   "quizId": 1436,
 *   "questionText": "다음 동작이 의미하는 단어는 무엇인가요?",
 *   "targetSignId": 1436,
 *   "image3dUrl": "https://sldict.korean.go.kr/.../IMG000224434_700X466.jpg",
 *   "options": ["결핵균", "모세", "스파게티", "모기"],
 *   "correctOptionIndex": 2
 * }
 *
 * ★ 주의 1 - correctOptionIndex 는 1-based (1~4) 다.
 *   실제 응답에서 options 가 4개인데 correctOptionIndex = 4 인 문제가 있었다.
 *   0-based 라면 나올 수 없는 값이므로 1-based 로 확정.
 *   그대로 쓰면 정답이 한 칸씩 밀리므로 Repository 에서 -1 해서 0-based 로 변환한다.
 *
 * ★ 주의 2 - image3dUrl 은 이름과 달리 3D 모델(.glb)이 아니라 그냥 JPG 다.
 *   국립국어원 한국수어사전 이미지 URL 이 내려온다. AsyncImage 로 띄우면 된다.
 */
data class QuizResponse(
    val quizId: Long,
    val questionText: String?,
    val targetSignId: Long?,
    val image3dUrl: String?,
    val options: List<String>?,
    val correctOptionIndex: Int?
)

/**
 * 퀴즈 답안 제출 요청 (POST /sign/language/quizzes/{quizId}/submit)
 *
 * body: { "selectedIndex": 1 }
 *
 * userId 는 보내지 않는다. 서버가 토큰으로 사용자를 식별한다.
 *
 * TODO(확인 필요): selectedIndex 도 correctOptionIndex 와 같은 1-based 로 보고 있다.
 *   Postman 에서 '퀴즈 답안 제출' 을 실제로 호출해서 확인할 것.
 */
data class QuizSubmitRequest(
    val selectedIndex: Int
)

/**
 * 퀴즈 채점 응답
 *
 * TODO(확인 필요): 실제 응답을 아직 못 받아봤다. 필드명이 다를 수 있어 전부 nullable 로 뒀다.
 *   (파싱이 어긋나도 앱이 죽지 않고 null 만 들어간다)
 *   UI 채점은 correctOptionIndex 로 로컬 판정하므로 이 DTO 가 틀려도 화면 동작에는 지장 없다.
 */
data class QuizSubmitResponse(
    val quizId: Long?,
    val isCorrect: Boolean?,
    val correctOptionIndex: Int?,
    val explanation: String?
)