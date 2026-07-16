package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.painterResource
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
                var selectedIndex by rememberSaveable { mutableIntStateOf(2) } // 홈이 기본

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = KuitTheme.colors.white
                        ) {
                            NavTab.entries.forEachIndexed { index, tab ->
                                val selected = selectedIndex == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { selectedIndex = index },
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = tab.icon),
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(text = tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = KuitTheme.colors.black,
                                        selectedTextColor = KuitTheme.colors.black,
                                        unselectedIconColor = KuitTheme.colors.gray1,
                                        unselectedTextColor = KuitTheme.colors.gray1,
                                        indicatorColor = KuitTheme.colors.main1
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedIndex) {
                            0 -> {}  // 수어 강의 (나중에)
                            1 -> QuizScreen()
                            2 -> HomeScreen()
                            3 -> {}  // 영상 통화 (나중에)
                            4 -> {}  // 프로필 (나중에)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}