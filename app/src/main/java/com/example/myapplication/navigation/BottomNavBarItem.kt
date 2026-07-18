package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

data class BottomNavBarItem(
    val icon: Int,
    val label: String,
    val route: Route
)

@Composable
fun NavItem(
    icon: Int,
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val selectedBackgroundColor=Color(0xFF363231)
    val unselectedColor = KuitTheme.colors.gray2
    val contentColor = if (isSelected) KuitTheme.colors.white else unselectedColor

    Box(
        modifier=modifier
            .height(55.dp)
            .clickable{onClick()},
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier= if (isSelected){
                Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(selectedBackgroundColor)
            }else {
                Modifier.size(55.dp)
            },
            contentAlignment = Alignment.Center
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Icon(
                    painter=painterResource(id=icon),
                    contentDescription = text,
                    modifier=Modifier.size(20.dp),
                    tint=contentColor
                )

                Spacer(modifier= Modifier.height(4.dp))

                Text(
                    text=text,
                    style= KuitTheme.typography.R_12,
                    color=contentColor
                )
            }
        }
    }
}