package com.example.myapplication.navigation

sealed interface Route{
    val route: String

    data object LOGIN: Route{
        override val route="login"
    }

    data object REGISTER_GRAPH: Route{
        override val route="register_graph"
    }

    data object REGISTER1: Route{
        override val route="register1"
    }

    data object REGISTER2: Route{
        override val route="register2"
    }

    data object HOME: Route{
        override val route="home"
    }

    data object LECTURE: Route{
        override val route="lecture"
    }

    data object LECTURE_DETAIL: Route{
        override val route="lecture_detail/{lectureId}"

        fun createRoute(lectureId:Long): String{
            return "lecture_detail/${lectureId}"
        }
    }

    data object QUIZ: Route{
        override val route="quiz"
    }

    data object QUIZ_DETAIL: Route{
        override val route="quiz_detail"
    }

    data object CALL: Route{
        override val route="call"
    }

    data object PROFILE: Route{
        override val route="profile"
    }

    data object SETTINGS: Route{
        override val route="settings"
    }

    data object PROFILE_EDIT: Route{
        override val route="profile_edit"
    }
}