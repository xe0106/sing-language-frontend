package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(
    navController: NavHostController
){
    val tabs= NavTab.entries

    val currentRoute=navController.currentBackStackEntryAsState().value?.destination?.route

    Row(
        modifier=Modifier
            .fillMaxWidth()
            .height(69.dp)
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ){
        tabs.forEach{tab->

            val isSelected=currentRoute==tab.route.route

            NavItem(
                icon=tab.icon,
                text=tab.label,
                isSelected=isSelected,
                onClick = {
                    navController.navigate(tab.route.route){
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop=true
                    }
                }
            )
        }
    }
}