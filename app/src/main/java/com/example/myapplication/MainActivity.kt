package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.MainNavHost
import com.example.myapplication.navigation.NavTab
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.quiz.QuizScreen
import com.example.myapplication.ui.theme.KuitTheme
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController= rememberNavController()
                val currentRoute=navController
                    .currentBackStackEntryAsState()
                    .value
                    ?.destination
                    ?.route

                val bottomNavRoutes= NavTab.entries.map{it.route.route}
                val showBottomBar=currentRoute in bottomNavRoutes

                Scaffold(
                    modifier=Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0),
                    bottomBar = {
                        if(showBottomBar) {
                            BottomNavBar(navController=navController)
                        }
                    }
                ) {
                    MainNavHost(
                        navController=navController,
                        modifier=Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}