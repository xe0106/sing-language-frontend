package com.example.myapplication.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit = {},
    onAppInfoClick: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // 로그아웃 / 탈퇴 완료 시 로그인 화면으로 이동
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                MyPageEvent.NavigateToLogin -> onLoggedOut()
            }
        }
    }

    val profile = uiState.profile
    val learningDays = profile?.learningDays ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.lecture_background),
                contentScale = ContentScale.Crop
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
                    text = "연속학습 $learningDays",
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
            // 프로필 이미지 (서버 URL, 없거나 실패하면 기본 캐릭터)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF6E8)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profile?.profileImageUrl,
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.character_profile),
                    error = painterResource(id = R.drawable.character_profile),
                    fallback = painterResource(id = R.drawable.character_profile),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 닉네임이 없으면 이름으로 대체
            Text(
                text = profile?.nickname ?: profile?.name ?: "사용자 이름",
                color = KuitTheme.colors.black,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.4).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = profile?.email ?: "-",
                color = Color(0xFFA3A3A3),
                fontSize = 12.sp,
                letterSpacing = (-0.24).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 연속 학습 박스
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
                    text = "${learningDays}일",
                    color = KuitTheme.colors.black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 요일 + 출석 도장 (서버 weeklyAttendance 사용)
                val attendanceList = profile?.weeklyAttendance.orEmpty()

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (attendanceList.isEmpty()) {
                        // 데이터가 아직 없을 때는 빈 도장으로 자리만 유지
                        listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                            AttendanceStamp(dayLabel = day, attended = false)
                        }
                    } else {
                        attendanceList.forEach { item ->
                            AttendanceStamp(
                                dayLabel = item.dayOfWeek.toKoreanDay(),
                                attended = item.attended == true
                            )
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
                onClick = { showLogoutDialog = true }
            )
            MyPageMenuItem(
                iconRes = R.drawable.ic_withdraw,
                text = "회원 탈퇴",
                onClick = { showWithdrawDialog = true }
            )
        }
    }

    // 로그아웃 확인 팝업
    if (showLogoutDialog) {
        ConfirmDialog(
            title = "로그아웃",
            message = "로그아웃 하시겠습니까?",
            confirmText = "로그아웃",
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // 회원 탈퇴 확인 팝업
    if (showWithdrawDialog) {
        ConfirmDialog(
            title = "회원 탈퇴",
            message = "탈퇴하면 학습 기록과 연락처가\n모두 삭제되며 복구할 수 없습니다.\n정말 탈퇴하시겠습니까?",
            confirmText = "탈퇴하기",
            onConfirm = {
                showWithdrawDialog = false
                viewModel.withdraw()
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }
}

/** 서버의 "MON" 형식을 "월"로 변환 */
private fun String?.toKoreanDay(): String = when (this) {
    "MON" -> "월"
    "TUE" -> "화"
    "WED" -> "수"
    "THU" -> "목"
    "FRI" -> "금"
    "SAT" -> "토"
    "SUN" -> "일"
    else -> "-"
}

@Composable
private fun AttendanceStamp(
    dayLabel: String,
    attended: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayLabel,
            color = if (attended) Color(0xFFFFB1B1) else Color(0xFFDDDDDD),
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (attended) Color(0xFFFFB1B1) else Color(0xFFEEEEEE)),
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

/**
 * 예 / 아니오를 묻는 공통 확인 팝업.
 */
@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = KuitTheme.colors.black,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "취소",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KuitTheme.colors.main1)
                        .clickable { onConfirm() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
