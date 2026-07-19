package com.example.myapplication.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun QuizBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .drawBehind {
                // 왼쪽 위 핑크 원
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFE4DA), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.15f),
                        radius = size.width * 0.7f
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.15f),
                    radius = size.width * 0.7f
                )
                // 오른쪽 아래 연노랑 원
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF0D1), Color.Transparent),
                        center = Offset(size.width * 0.75f, size.height * 0.75f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.75f, size.height * 0.75f),
                    radius = size.width * 0.8f
                )
            },
        content = content
    )
}