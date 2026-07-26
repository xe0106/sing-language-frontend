package com.example.myapplication.ui.call.video_call.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.call.video_call.CallMessage
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallMessagePanel(
    messages: List<CallMessage>,
    input: String,
    onInputChange: (String)->Unit,
    onSendClick: ()->Unit,
    modifier: Modifier= Modifier
){
    val panelShape = RoundedCornerShape(24.dp)

    Surface(
        modifier=modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp),
        color=KuitTheme.colors.white.copy(alpha = 0.16f),
        shape = panelShape,
        border = BorderStroke(
            width = 1.dp,
            color = KuitTheme.colors.white.copy(alpha = 0.25f)
        )
    ){
        Column(
            modifier= Modifier.padding(16.dp)
        ){
            LazyColumn(
                modifier= Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ){
                items(messages, key={it.id}){message->
                    Row(
                        modifier= Modifier.fillMaxWidth(),
                        horizontalArrangement = if(message.isMine) {
                            Arrangement.End
                        } else {
                            Arrangement.Start
                        }
                    ){
                        Surface(
                            color = KuitTheme.colors.white,
                            shape = RoundedCornerShape(60.dp)
                        ) {
                            Text(
                                text=message.text,
                                modifier= Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = KuitTheme.typography.R_12
                            )
                        }
                    }
                }
            }

            TextField(
                value=input,
                onValueChange = onInputChange,
                modifier= Modifier.fillMaxWidth(),
                placeholder = {
                    Text("메시지를 입력하세요")
                },
                trailingIcon = {
                    IconButton(onClick=onSendClick) {
                        Icon(
                            painter = painterResource(R.drawable.send),
                            contentDescription = "전송",
                            tint = KuitTheme.colors.white
                        )
                    }
                },
                singleLine = true,
                shape= RoundedCornerShape(60.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor =
                        KuitTheme.colors.white.copy(alpha = 0.3f),
                    unfocusedContainerColor =
                        KuitTheme.colors.white.copy(alpha = 0.3f),
                    disabledContainerColor =
                        KuitTheme.colors.white.copy(alpha = 0.3f),

                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,

                    focusedTextColor = KuitTheme.colors.white,
                    unfocusedTextColor = KuitTheme.colors.white,
                    cursorColor = KuitTheme.colors.white,
                    focusedPlaceholderColor =
                        KuitTheme.colors.white.copy(alpha = 0.7f),
                    unfocusedPlaceholderColor =
                        KuitTheme.colors.white.copy(alpha = 0.7f)
                )
            )
        }
    }
}