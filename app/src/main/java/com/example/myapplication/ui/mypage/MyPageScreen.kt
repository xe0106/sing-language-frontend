package com.example.myapplication.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun MyPageScreen(
    onSettingsClick: () -> Unit = {},
    onAppInfoClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {}
) {
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
            .padding(16.dp)
    ) {
        // 상단: 연속학습 배지 + 설정 톱니바퀴
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(35.dp)
                    .clip(RoundedCornerShape(1000.dp))
                    .background(Color(0x33F3F4F6))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "연속학습 12",
                    color = KuitTheme.colors.black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color(0x33F9FAFB))
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "설정",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 프로필 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF6E8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.character_profile),
                    contentDescription = "프로필 캐릭터",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TODO: 서버 연동 시 ViewModel 값으로 교체
            Text(
                text = "사용자 이름",
                color = KuitTheme.colors.black,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.4).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "user@example.com",
                color = Color(0xFFA3A3A3),
                fontSize = 12.sp,
                letterSpacing = (-0.24).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 연속 학습 박스 (테두리 있음)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFDDDDDD),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(Color.White)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "연속 학습",
                    color = Color(0xFFA3A3A3),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "12일",
                    color = KuitTheme.colors.black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 요일 + 출석 도장 (TODO: 서버 연동 시 실제 데이터로 교체)
                val days = listOf("월", "화", "수", "목", "금", "토", "일")
                val attendance = listOf(true, true, true, false, false, false, false)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.zip(attendance).forEach { (day, attended) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day,
                                color = if (attended) Color(0xFFFFB1B1)
                                else Color(0xFFDDDDDD),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (attended) Color(0xFFFFB1B1)
                                        else Color(0xFFEEEEEE)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (attended) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 메뉴 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .padding(vertical = 4.dp)
        ) {
            MyPageMenuItem(
                iconRes = R.drawable.ic_info,
                text = "앱 정보",
                onClick = onAppInfoClick
            )
            MyPageMenuItem(
                iconRes = R.drawable.ic_logout,
                text = "로그아웃",
                onClick = onLogoutClick
            )
            MyPageMenuItem(
                iconRes = R.drawable.ic_withdraw,
                text = "회원 탈퇴",
                onClick = onWithdrawClick
            )
        }
    }
}

@Composable
private fun MyPageMenuItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = KuitTheme.colors.black,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MyPageScreen()
}