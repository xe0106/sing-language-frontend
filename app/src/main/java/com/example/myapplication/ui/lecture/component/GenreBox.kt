package com.example.myapplication.ui.lecture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

data class Genre(
    val name:String
)

@Composable
fun GenreBox(
    modifier: Modifier= Modifier,
    genres: List<Genre>,
    selectedGenre:Genre,
    onGenreClick:(Genre)->Unit
){
    LazyRow(
        modifier=modifier
            .height(36.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(genres){genre->
            val isSelected=genre==selectedGenre

            Box(
                modifier=modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        if(isSelected) KuitTheme.colors.black
                        else Color(0xFFF2D3CE)
                    )
                    .clickable{
                        onGenreClick(genre)
                    },
                contentAlignment = Alignment.Center
            ){
                Text(
                    text=genre.name,
                    color=if(isSelected) KuitTheme.colors.white else KuitTheme.colors.black,
                    style = KuitTheme.typography.M_14
                )
            }
        }
    }
}