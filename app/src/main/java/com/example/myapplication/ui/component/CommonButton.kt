package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CommonButton(
    onClick: () -> Unit,
    buttonName: String,
    bgColor: Color= KuitTheme.colors.black,
    textColor: Color= KuitTheme.colors.white,
    textStyle: TextStyle= KuitTheme.typography.B_18,
    width: Dp=361.dp,
    height: Dp=70.dp,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .width(width)
            .height(height),

        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            disabledContainerColor = bgColor,
            disabledContentColor = textColor
        ),
        shape = RoundedCornerShape(10000.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = buttonName,
                style = textStyle,
                color = textColor
            )
        }
    }
}
