package com.example.myapplication.ui.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.component.CommonTextField
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CommonTextBox(
    des: String,
    phd: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None
){
    Text(
        modifier=modifier
            .width(345.dp)
            .height(20.dp),
        text=des,
        color=Color(0XFF364153),
        style= KuitTheme.typography.R_14
    )

    Spacer(modifier= Modifier.height(4.dp))

    CommonTextField(
        value = value,
        onValueChange = onValueChange,
        placeHolder = phd,
        visualTransformation = visualTransformation
    )
}
