package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.myapplication.ui.call.call_home.CallScreen
import com.example.myapplication.ui.call.call_receive.CallReceiveScreen
import com.example.myapplication.ui.call.video_call.VideoCallScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.lecture.LectureDetailScreen
import com.example.myapplication.ui.lecture.LectureScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.mypage.MyPageScreen
import com.example.myapplication.ui.mypage.ProfileEditScreen
import com.example.myapplication.ui.quiz.QuizScreen
import com.example.myapplication.ui.register.RegisterScreen1
import com.example.myapplication.ui.register.RegisterScreen2
import com.example.myapplication.ui.register.RegisterViewModel
import com.example.myapplication.ui.settings.SettingsScreen

/** 퀴즈 완료 결과를 홈 화면으로 돌려줄 때 사용하는 키 */
const val RESULT_QUIZ_COMPLETED = "result_quiz_completed"

/**
 * 바텀 네비게이션 탭 이동 전용 함수.
 * popUpTo 가 먼저 실행되어 HOME 위에 쌓인 화면이 제거되고,
 * 그 다음 launchSingleTop 이 중복 push 를 막는다.
 */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Route.HOME.route) { inclusive = false }
        launchSingleTop = true
        restoreState = false
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.AUTH_GRAPH.route,
        modifier = modifier
    ) {

        // ---------------- 인증 그래프 ----------------
        navigation(
            route = Route.AUTH_GRAPH.route,
            startDestination = Route.LOGIN.route
        ) {
            composable(Route.LOGIN.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Route.MAIN_GRAPH.route) {
                            popUpTo(Route.AUTH_GRAPH.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Route.REGISTER_GRAPH.route)
                    }
                )
            }

            navigation(
                route = Route.REGISTER_GRAPH.route,
                startDestination = Route.REGISTER1.route
            ) {
                composable(Route.REGISTER1.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Route.REGISTER_GRAPH.route)
                    }
                    val registerViewModel: RegisterViewModel = hiltViewModel(parentEntry)

                    RegisterScreen1(
                        viewModel = registerViewModel,
                        onNextClick = { navController.navigate(Route.REGISTER2.route) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Route.REGISTER2.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Route.REGISTER_GRAPH.route)
                    }
                    val registerViewModel: RegisterViewModel = hiltViewModel(parentEntry)

                    RegisterScreen2(
                        viewModel = registerViewModel,
                        onRegisterSuccess = {
                            navController.navigate(Route.LOGIN.route) {
                                popUpTo(Route.REGISTER_GRAPH.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        // ---------------- 메인 그래프 (startDestination = HOME 이 핵심) ----------------
        navigation(
            route = Route.MAIN_GRAPH.route,
            startDestination = Route.HOME.route
        ) {

            composable(Route.HOME.route) { entry ->
                val homeViewModel: HomeViewModel = hiltViewModel()

                // 퀴즈 완료 신호를 받으면 홈 진도율을 다시 불러온다
                LaunchedEffect(Unit) {
                    entry.savedStateHandle
                        .getStateFlow(RESULT_QUIZ_COMPLETED, false)
                        .collect { completed ->
                            if (completed) {
                                homeViewModel.refresh()
                                entry.savedStateHandle[RESULT_QUIZ_COMPLETED] = false
                            }
                        }
                }

                HomeScreen(
                    viewModel = homeViewModel,
                    onLectureClick = { navController.navigate(Route.LECTURE.route) },
                    onQuizClick = { navController.navigate(Route.QUIZ.route) },
                    onCallClick = { navController.navigate(Route.CALL.route) }
                )
            }

            composable(Route.QUIZ.route) {
                QuizScreen(
                    onBackClick = { navController.popBackStack() },
                    onQuizFinished = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(RESULT_QUIZ_COMPLETED, true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Route.LECTURE.route) {
                LectureScreen(
                    onLectureClick = { lectureId ->
                        navController.navigate(Route.LECTURE_DETAIL.createRoute(lectureId))
                    }
                )
            }

            composable(Route.LECTURE_DETAIL.route) { backStackEntry ->
                val lectureId = backStackEntry.arguments
                    ?.getString("lectureId")
                    ?.toLongOrNull()

                if (lectureId != null) {
                    LectureDetailScreen(
                        lectureId = lectureId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Route.CALL.route) {
                CallScreen(
                    onSettingsClick = { navController.navigate(Route.SETTINGS.route) },
                    onContactClick = { contact ->
                        // TODO: 발신 API 응답의 UUID callId로 교체
                        navController.navigate(Route.VIDEO_CALL.createRoute(contact.name))
                    }
                )
            }

            composable(Route.CALL_RECEIVE.route) { backStackEntry ->
                val callId = backStackEntry.arguments
                    ?.getString("callId")

                if (callId != null) {
                    CallReceiveScreen(
                        callId = callId,
                        onAcceptSuccess = {
                            navController.navigate(Route.VIDEO_CALL.createRoute(callId)) {
                                popUpTo(Route.CALL_RECEIVE.route) { inclusive = true }
                            }
                        },
                        onRejectSuccess = {
                            navController.navigate(Route.HOME.route) {
                                popUpTo(Route.CALL_RECEIVE.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(Route.VIDEO_CALL.route) { backStackEntry ->
                val callId = backStackEntry.arguments
                    ?.getString("callId")

                if (callId != null) {
                    VideoCallScreen(
                        callId = callId,
                        onCallEnded = {
                            navController.navigate(Route.CALL.route) {
                                popUpTo(Route.VIDEO_CALL.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(Route.PROFILE.route) {
                MyPageScreen(
                    onSettingsClick = { navController.navigate(Route.SETTINGS.route) },
                    onLoggedOut = {
                        navController.navigate(Route.AUTH_GRAPH.route) {
                            popUpTo(Route.MAIN_GRAPH.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Route.SETTINGS.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onProfileEditClick = { navController.navigate(Route.PROFILE_EDIT.route) }
                )
            }

            composable(Route.PROFILE_EDIT.route) {
                ProfileEditScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
