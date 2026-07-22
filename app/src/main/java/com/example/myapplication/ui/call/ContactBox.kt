package com.example.myapplication.ui.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun ContactBox(
    modifier: Modifier =Modifier,
    contacts: List<Contact>,
    onContactClick: (Contact)->Unit
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(KuitTheme.colors.white),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 24.dp,
            end = 24.dp,
            bottom = 24.dp
        )
    ) {
        // 제목은 한 개이므로 item 사용
        item {
            Text(
                text = "연락처",
                color = KuitTheme.colors.black,
                style = KuitTheme.typography.SB_18
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 연락처 목록은 items 사용
        itemsIndexed(
            items = contacts,
            key = { _, contact -> contact.id }
        ) { index, contact ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (contact.profileImageUrl != null) {
                        AsyncImage(
                            model = contact.profileImageUrl,
                            contentDescription = "${contact.name} 프로필 이미지",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = R.drawable.basic_profile
                            ),
                            contentDescription = "기본 프로필 이미지",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = contact.name,
                        color = KuitTheme.colors.black,
                        style = KuitTheme.typography.R_14
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_phone),
                    contentDescription = "${contact.name}에게 전화",
                    tint = KuitTheme.colors.gray1,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            onContactClick(contact)
                        }
                )
            }

            // 마지막 연락처가 아닐 때만 간격 추가
            if (index < contacts.lastIndex) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}