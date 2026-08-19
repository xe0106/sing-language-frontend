package com.example.myapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.component.TopBar
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLectureClick: () -> Unit = {},
    onQuizClick: () -> Unit = {},
    onCallClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KuitTheme.colors.main2)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(27.dp))

        TopBar(title = "홈")

        // 상단 인사말 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(KuitTheme.colors.white)
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.currentDate,
                        color = KuitTheme.colors.gray1,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.greetingMessage,
                        color = KuitTheme.colors.black,
                        fontSize = 20.sp
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.character_home),
                    contentDescription = "캐릭터",
                    modifier = Modifier.size(width = 157.dp, height = 141.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.goalTitle,
                color = KuitTheme.colors.black,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth(),
                color = KuitTheme.colors.main1,
                trackColor = KuitTheme.colors.gray2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(uiState.progress * 100).toInt()}%",
                color = KuitTheme.colors.gray1,
                fontSize = 12.sp
            )
        }

        // 메뉴 버튼 3개
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                Triple(R.drawable.ic_class_image, "수어 강의", onLectureClick),
                Triple(R.drawable.ic_quiz_image, "수어 퀴즈", onQuizClick),
                Triple(R.drawable.ic_call_image, "영상 통화", onCallClick)
            ).forEach { (icon, label, onClick) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                        .background(KuitTheme.colors.white)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = KuitTheme.colors.black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 최근 연락처 (서버 데이터)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFFFF).copy(alpha = 0.3f))
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "최근 연락처",
                color = KuitTheme.colors.black,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.recentContacts.isEmpty()) {
                Text(
                    text = "최근 연락처가 없습니다",
                    color = KuitTheme.colors.gray1,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            } else {
                uiState.recentContacts.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCallClick() }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = contact.profileImageUrl,
                                contentDescription = contact.contactName,
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.basic_profile),
                                error = painterResource(id = R.drawable.basic_profile),
                                fallback = painterResource(id = R.drawable.basic_profile),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = contact.contactName,
                                color = KuitTheme.colors.black,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "전화",
                            tint = KuitTheme.colors.gray1,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    HorizontalDivider(
                        color = Color(0xFFF9FAFB),
                        thickness = 0.75.dp
                    )
                }
            }
        }
    }
}