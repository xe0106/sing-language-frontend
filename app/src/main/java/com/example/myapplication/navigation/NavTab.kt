package com.example.myapplication.navigation

import com.example.myapplication.R

enum class NavTab(
    val icon: Int,
    val label: String,
    val route: Route
){
    CLASS(
        icon = R.drawable.ic_class,
        label = "수어 강의",
        route = Route.CLASS
    ),
    QUIZ(
        icon = R.drawable.ic_quiz,
        label = "수어 퀴즈",
        route = Route.QUIZ
    ),
    HOME(
        icon = R.drawable.ic_home,
        label = "홈",
        route = Route.HOME
    ),
    CALL(
        icon = R.drawable.ic_call,
        label = "영상 통화",
        route = Route.CALL
    ),
    PROFILE(
        icon = R.drawable.ic_profile,
        label = "프로필",
        route = Route.PROFILE
    )
}