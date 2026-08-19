package com.example.myapplication.ui.call.video_call.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
    val messageListState = rememberLazyListState()

    LaunchedEffect(messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) {
            messageListState.animateScrollToItem(
                index = messages.lastIndex
            )
        }
    }

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
                state = messageListState,
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

            if (messages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    textStyle = KuitTheme.typography.R_14.copy(
                        color = KuitTheme.colors.white
                    ),
                    cursorBrush = SolidColor(KuitTheme.colors.white),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSendClick() }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(15.dp))
                                .background(
                                    KuitTheme.colors.white.copy(
                                        alpha = 0.3f
                                    )
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (input.isEmpty()) {
                                Text(
                                    text = "메시지를 입력하세요",
                                    color = KuitTheme.colors.white.copy(
                                        alpha = 0.7f
                                    ),
                                    style = KuitTheme.typography.R_14
                                )
                            }

                            innerTextField()
                        }
                    }
                )

                Image(
                    painter = painterResource(R.drawable.send),
                    contentDescription = "전송",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSendClick)
                )
            }
        }
    }
}
