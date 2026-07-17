package com.example.myapplication.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.lecture.LectureScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.quiz.QuizScreen
import com.example.myapplication.ui.register.RegisterScreen1
import com.example.myapplication.ui.register.RegisterScreen2

@Composable
fun MainNavHost(
    padding: PaddingValues,
    navController: NavHostController
){
    NavHost(
        navController=navController,
        startDestination = Route.LOGIN.route,
        modifier = Modifier.padding(padding)
    ){
        composable(Route.LOGIN.route){
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.HOME.route){
                        popUpTo(Route.LOGIN.route){
                            inclusive=true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Route.REGISTER1.route)
                }
            )
        }

        composable(Route.REGISTER1.route) {
            RegisterScreen1(
                onNextClick = {
                    navController.navigate(Route.REGISTER2.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.REGISTER2.route) {
            RegisterScreen2(
                onRegisterSuccess = {
                    navController.navigate(Route.LOGIN.route) {
                        popUpTo(Route.REGISTER1.route) {
                            inclusive = true
                        }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.HOME.route) {
            HomeScreen()
        }

        composable(Route.QUIZ.route) {
            QuizScreen()
        }

        composable(Route.LECTURE.route) {
            LectureScreen()
        }

        composable(Route.CALL.route) {
            // CallScreen()
        }

        composable(Route.PROFILE.route) {
            // ProfileScreen()
        }
    }
}