package com.example.myapplication.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit = {}
) {
    var nickname by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var birth by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFCFC2),
                        Color(0xFFFFE9D6),
                        Color(0xFFFFF6E8)
                    )
                )
            )
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "프로필 수정",
                color = KuitTheme.colors.black,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "✕",
                color = KuitTheme.colors.black,
                fontSize = 20.sp,
                modifier = Modifier.clickable { onBackClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 프로필 이미지 (회색 실루엣 + 수정 연필)
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_placeholder),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.size(48.dp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_pen),
                contentDescription = "프로필 사진 수정",
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .clickable {
                        // TODO: 프로필 사진 변경 기능 (기획 확인 필요)
                    }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 닉네임 (중복확인 버튼 포함)
            Column {
                Text(
                    text = "닉네임",
                    color = KuitTheme.colors.black,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 0.75.dp,
                            color = Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (nickname.isEmpty()) {
                            Text(
                                text = "닉네임",
                                color = Color(0xFFA3A3A3),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            textStyle = TextStyle(
                                color = KuitTheme.colors.black,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = "중복확인",
                        color = Color(0xFF999999),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            // TODO: API 연동 시 닉네임 중복확인 요청
                        }
                    )
                }
            }

            // 성별
            Column {
                Text(
                    text = "성별",
                    color = KuitTheme.colors.black,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(4.dp)
                ) {
                    GenderButton(
                        text = "남성",
                        selected = isMale,
                        modifier = Modifier.weight(1f),
                        onClick = { isMale = true }
                    )
                    GenderButton(
                        text = "여성",
                        selected = !isMale,
                        modifier = Modifier.weight(1f),
                        onClick = { isMale = false }
                    )
                }
            }

            // 생년월일
            ProfileEditField(
                label = "생년월일",
                value = birth,
                placeholder = "사용자 입력",
                onValueChange = { birth = it }
            )

            // 전화번호
            ProfileEditField(
                label = "전화번호",
                value = phone,
                placeholder = "사용자 입력",
                onValueChange = { phone = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = KuitTheme.colors.black,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 0.75.dp,
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFFA3A3A3),
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = KuitTheme.colors.black,
                    fontSize = 14.sp
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GenderButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) KuitTheme.colors.black else Color(0xFFA3A3A3),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen()
}