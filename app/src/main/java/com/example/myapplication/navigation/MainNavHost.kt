package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.lecture.LectureScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.mypage.MyPageScreen
import com.example.myapplication.ui.mypage.ProfileEditScreen
import com.example.myapplication.ui.quiz.QuizScreen
import com.example.myapplication.ui.register.RegisterScreen1
import com.example.myapplication.ui.register.RegisterScreen2
import com.example.myapplication.ui.register.RegisterViewModel
import com.example.myapplication.ui.settings.SettingsScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.LOGIN.route,
        modifier = modifier
    ) {
        composable(Route.LOGIN.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.HOME.route) {
                        popUpTo(Route.LOGIN.route) {
                            inclusive = true
                        }
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
                    onNextClick = {
                        navController.navigate(Route.REGISTER2.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
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
                            popUpTo(Route.REGISTER_GRAPH.route) {
                                inclusive = true
                            }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Route.HOME.route) {
            HomeScreen()
        }

        composable(Route.QUIZ.route) {
            QuizScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.LECTURE.route) {
            LectureScreen()
        }

        composable(Route.CALL.route) {
            // CallScreen()
        }

        composable(Route.PROFILE.route) {
            MyPageScreen(
                onSettingsClick = {
                    navController.navigate(Route.SETTINGS.route)
                },
                onLogoutClick = {
                    // TODO: 로그아웃 처리 후 로그인 화면으로
                },
                onWithdrawClick = {
                    // TODO: 회원 탈퇴 처리
                }
            )
        }

        composable(Route.SETTINGS.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileEditClick = {
                    navController.navigate(Route.PROFILE_EDIT.route)
                }
            )
        }

        composable(Route.PROFILE_EDIT.route) {
            ProfileEditScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}