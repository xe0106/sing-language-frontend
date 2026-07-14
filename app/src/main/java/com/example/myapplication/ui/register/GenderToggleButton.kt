package com.example.myapplication.ui.register

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

enum class Gender {
    MALE, FEMALE
}

@Composable
fun GenderToggleButton(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetX by animateDpAsState(
        targetValue = if (selectedGender == Gender.MALE) 0.dp else 172.5.dp,
        label = "genderToggleOffset"
    )

    Box(
        modifier = modifier
            .width(345.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3F4F6))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(169.dp)
                .height(40.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(KuitTheme.colors.white)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenderToggleItem(
                text = "남성",
                selected = selectedGender == Gender.MALE,
                modifier = Modifier.weight(1f),
                onClick = { onGenderSelected(Gender.MALE) }
            )

            GenderToggleItem(
                text = "여성",
                selected = selectedGender == Gender.FEMALE,
                modifier = Modifier.weight(1f),
                onClick = { onGenderSelected(Gender.FEMALE) }
            )
        }
    }
}

@Composable
private fun GenderToggleItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) KuitTheme.colors.black else Color(0xFF6A7282),
            style = KuitTheme.typography.M_14
        )
    }
}