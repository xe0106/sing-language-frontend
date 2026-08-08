package com.example.myapplication.ui.lecture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme


@Composable
fun GenreBox(
    modifier: Modifier= Modifier,
    categories: List<LectureCategory>,
    selectedCategory: LectureCategory,
    onCategoryClick:(LectureCategory)->Unit
){
    LazyRow(
        modifier=modifier
            .height(36.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories){ category->
            val isSelected=category==selectedCategory

            Box(
                modifier=Modifier
                    .height(36.dp)
                    .widthIn(min = 72.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        if(isSelected) KuitTheme.colors.black
                        else Color(0xFFF2D3CE)
                    )
                    .clickable{
                        onCategoryClick(category)
                    }
                    .padding(horizontal = 17.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text=category.displayName,
                    color=if(isSelected) KuitTheme.colors.white else KuitTheme.colors.black,
                    style = KuitTheme.typography.M_14,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}