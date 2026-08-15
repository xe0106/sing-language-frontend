package com.example.myapplication.navigation

enum class Route(val route: String) {

    // 그래프
    AUTH_GRAPH("auth_graph"),
    MAIN_GRAPH("main_graph"),
    REGISTER_GRAPH("register_graph"),

    // 인증
    LOGIN("login"),
    REGISTER1("register1"),
    REGISTER2("register2"),

    // 메인 탭
    HOME("home"),
    LECTURE("lecture"),
    CALL("call"),
    PROFILE("profile"),

    // 상세 화면
    QUIZ("quiz"),
    LECTURE_DETAIL("lecture_detail/{lectureId}"),
    CALL_RECEIVE("call_receive/{callId}"),
    VIDEO_CALL("video_call/{callId}"),
    SETTINGS("settings"),
    PROFILE_EDIT("profile_edit");

    /** 인자가 있는 화면으로 이동할 때 사용 */
    fun createRoute(id: Long): String = when (this) {
        LECTURE_DETAIL -> "lecture_detail/$id"
        CALL_RECEIVE -> "call_receive/$id"
        VIDEO_CALL -> "video_call/$id"
        else -> route
    }
}