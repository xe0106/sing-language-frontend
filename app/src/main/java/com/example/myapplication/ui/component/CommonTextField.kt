package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CommonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeHolder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
){
    TextField(
        value=value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text=placeHolder,
                color= KuitTheme.colors.gray2,
                style = KuitTheme.typography.M_14
            )
        },
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        shape= RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = KuitTheme.colors.white,
            unfocusedContainerColor = KuitTheme.colors.white,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = KuitTheme.colors.main1
        ),
        modifier=modifier
            .width(345.dp)
            .height(50.dp)
    )
}
