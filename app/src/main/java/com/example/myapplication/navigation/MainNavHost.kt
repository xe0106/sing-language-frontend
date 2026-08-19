package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.myapplication.ui.call.IncomingCallViewModel
import com.example.myapplication.ui.call.call_home.CallScreen
import com.example.myapplication.ui.call.call_receive.CallReceiveScreen
import com.example.myapplication.ui.call.video_call.VideoCallScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.lecture.LectureDetailScreen
import com.example.myapplication.ui.lecture.LectureScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.mypage.MyPageScreen
import com.example.myapplication.ui.mypage.MyPageViewModel
import com.example.myapplication.ui.mypage.ProfileEditScreen
import com.example.myapplication.ui.quiz.QuizScreen
import com.example.myapplication.ui.quiz.QuizViewModel
import com.example.myapplication.ui.register.RegisterScreen1
import com.example.myapplication.ui.register.RegisterScreen2
import com.example.myapplication.ui.register.RegisterViewModel
import com.example.myapplication.ui.settings.SettingsScreen

/** 퀴즈 완료 결과를 홈 화면으로 돌려줄 때 사용하는 키 */
const val RESULT_QUIZ_COMPLETED = "result_quiz_completed"

/** 프로필 수정 완료 후 마이페이지 데이터를 다시 불러오기 위한 키 */
const val RESULT_PROFILE_UPDATED = "result_profile_updated"

/**
 * 바텀 네비게이션 탭 이동 전용 함수.
 * HOME 은 저장된 탭 화면을 복원하지 않고 HOME 까지 백스택을 정리한다.
 * 다른 탭은 각 탭의 저장된 상태를 복원한다.
 */
fun NavHostController.navigateToTab(route: String) {
    if (route == Route.HOME.route) {
        val poppedToHome = popBackStack(
            route = Route.HOME.route,
            inclusive = false
        )

        if (
            !poppedToHome &&
            currentDestination?.route != Route.HOME.route
        ) {
            navigate(Route.HOME.route) {
                launchSingleTop = true
                restoreState = false
            }
        }

        return
    }

    navigate(route) {
        popUpTo(Route.HOME.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val incomingCallViewModel: IncomingCallViewModel =
        hiltViewModel()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        incomingCallViewModel
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                incomingCallViewModel.ensureListening()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(
        navController,
        incomingCallViewModel
    ) {
        incomingCallViewModel.incomingCalls.collect { incomingCall ->
            val currentRoute =
                navController.currentDestination?.route

            if (
                currentRoute == Route.LOGIN.route ||
                currentRoute == Route.REGISTER1.route ||
                currentRoute == Route.REGISTER2.route ||
                currentRoute == Route.CALL_RECEIVE.route ||
                currentRoute == Route.VIDEO_CALL.route
            ) {
                return@collect
            }

            navController.navigate(
                Route.CALL_RECEIVE.createRoute(
                    incomingCall.callId
                )
            ) {
                launchSingleTop = true
            }
        }
    }

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
                        incomingCallViewModel.startListening()

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
                    onLectureClick = {
                        navController.navigateToTab(Route.LECTURE.route)
                    },
                    onQuizClick = {
                        navController.navigateToTab(Route.QUIZ.route)
                    },
                    onCallClick = {
                        navController.navigateToTab(Route.CALL.route)
                    }
                )
            }

            composable(Route.QUIZ.route) { backStackEntry ->
                // 퀴즈 화면이 HOME 이동으로 백스택에서 제거되어도
                // 진행 상태는 MAIN_GRAPH가 유지되는 동안 보존한다.
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(
                        Route.MAIN_GRAPH.route
                    )
                }
                val quizViewModel: QuizViewModel =
                    hiltViewModel(parentEntry)

                QuizScreen(
                    viewModel = quizViewModel,
                    onBackClick = { navController.popBackStack() },
                    onQuizFinished = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(RESULT_QUIZ_COMPLETED, true)

                        // 완료한 문제 세트는 다음 퀴즈 진입 때 반복하지 않는다.
                        quizViewModel.restart()
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
                    onCallStarted = { callId ->
                        navController.navigate(
                            Route.VIDEO_CALL.createRoute(callId)
                        )
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

            composable(Route.PROFILE.route) { entry ->
                val myPageViewModel: MyPageViewModel = hiltViewModel()

                LaunchedEffect(Unit) {
                    entry.savedStateHandle
                        .getStateFlow(RESULT_PROFILE_UPDATED, false)
                        .collect { updated ->
                            if (updated) {
                                myPageViewModel.loadProfile()
                                entry.savedStateHandle[RESULT_PROFILE_UPDATED] = false
                            }
                        }
                }

                MyPageScreen(
                    viewModel = myPageViewModel,
                    onSettingsClick = { navController.navigate(Route.SETTINGS.route) },
                    onLoggedOut = {
                        incomingCallViewModel.stopListening()

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
                    onBackClick = { navController.popBackStack() },
                    onUpdateSuccess = {
                        navController.getBackStackEntry(Route.PROFILE.route)
                            .savedStateHandle[RESULT_PROFILE_UPDATED] = true
                        navController.popBackStack(
                            route = Route.PROFILE.route,
                            inclusive = false
                        )
                    }
                )
            }
        }
    }
}
