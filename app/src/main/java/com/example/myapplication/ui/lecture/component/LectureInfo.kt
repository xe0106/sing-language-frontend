package com.example.myapplication.ui.lecture.component

import android.R.attr.text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun LectureInfo(
    modifier: Modifier= Modifier,
    lecture: Lecture
){
    Column(
        modifier=modifier
            .width(159.dp)
            .height(100.dp)
    ){
        Text(
            modifier=Modifier
                .fillMaxWidth(),
                //.height(45.dp),
            color = Color(0XFF212121),
            style = KuitTheme.typography.B_18,
            text=lecture.title
        )

        Text(
            modifier=Modifier
                .fillMaxWidth(),
                //.height(55.dp),
            color = Color(0XFF96979B),
            style = KuitTheme.typography.M_12,
            text=lecture.description
        )

        /*Text(
            modifier=Modifier
                .fillMaxWidth()
                .height(22.dp),
            color = Color(0XFF96979B),
            style = KuitTheme.typography.R_12,
            text="${lecture.time}초"
        )*/
    }
}