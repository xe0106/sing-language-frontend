package com.example.myapplication.ui.call.call_receive

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallReceiveButton(
    onClick: () -> Unit,
    buttonName: String,
    bgColor: Color=KuitTheme.colors.black,
    textColor: Color= KuitTheme.colors.white,
    textStyle: TextStyle= KuitTheme.typography.B_18,
    icon: Int,
    width: Dp=361.dp,
    height: Dp=70.dp,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .height(height),

        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor
        ),
        shape = RoundedCornerShape(10000.dp)
    ) {

        Icon(
            painter=painterResource(id=icon),
            contentDescription = null,
            modifier=Modifier.size(20.dp),
        )

        Spacer(modifier=Modifier.width(8.dp))

        Text(
            text = buttonName,
            style = textStyle,
            color = textColor
        )
    }
}