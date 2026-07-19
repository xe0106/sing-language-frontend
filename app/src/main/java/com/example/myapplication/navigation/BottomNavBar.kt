package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun BottomNavBar(
    navController: NavHostController
){
    val tabs= NavTab.entries

    val currentRoute=navController.currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 16.dp)
            .height(69.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(1000.dp),
                clip = false
            )
            .clip(RoundedCornerShape(1000.dp))
            .background(KuitTheme.colors.white),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        tabs.forEach{tab->

            val isSelected=currentRoute==tab.route.route

            NavItem(
                icon=tab.icon,
                text=tab.label,
                isSelected=isSelected,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (!isSelected) {
                        navController.navigate(tab.route.route) {
                            popUpTo(Route.HOME.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}