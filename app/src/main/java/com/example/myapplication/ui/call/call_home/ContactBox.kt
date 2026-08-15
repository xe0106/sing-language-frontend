package com.example.myapplication.ui.call.call_home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

@Composable
fun ContactBox(
    modifier: Modifier =Modifier,
    contacts: List<Contact>,
    onContactClick: (Contact)->Unit,
    onDeleteContact: (Contact)->Unit,
    onAddContactClick: () -> Unit
){
    var openedContactId by remember {
        mutableStateOf<Long?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 649.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(KuitTheme.colors.white)
    ) {
        Text(
            text = "연락처",
            color = KuitTheme.colors.black,
            style = KuitTheme.typography.SB_18,
            modifier = Modifier.padding(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f, fill = false),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            )
        ) {
            itemsIndexed(
                items = contacts,
                key = { _, contact -> contact.contactId }
            ) { index, contact ->

                SwipeToDeleteContactItem(
                    contact = contact,
                    isOpen = openedContactId == contact.contactId,
                    onOpenChange = { shouldOpen ->
                        if (shouldOpen) {
                            openedContactId = contact.contactId
                        } else if (openedContactId == contact.contactId) {
                            openedContactId = null
                        }
                    },
                    onContactClick = onContactClick,
                    onDeleteClick = { selectedContact ->
                        openedContactId = null
                        onDeleteContact(selectedContact)
                    }
                )

                if (index < contacts.lastIndex) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        HorizontalDivider(
            color = KuitTheme.colors.gray2,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable(onClick = onAddContactClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = KuitTheme.colors.gray1,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = KuitTheme.colors.gray1,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "연락처 추가",
                color = KuitTheme.colors.gray1,
                style = KuitTheme.typography.R_14
            )
        }
    }
}

@Composable
private fun SwipeToDeleteContactItem(
    contact: Contact,
    isOpen: Boolean,
    onOpenChange: (Boolean)->Unit,
    onContactClick: (Contact) -> Unit,
    onDeleteClick: (Contact) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val deleteWidth = 80.dp
    val deleteWidthPx = with(density) {
        deleteWidth.toPx()
    }

    val offsetX = remember(contact.contactId) {
        Animatable(0f)
    }

    LaunchedEffect(isOpen, deleteWidthPx) {
        offsetX.animateTo(
            if (isOpen) -deleteWidthPx else 0f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEB5757))
                .clickable {
                    onDeleteClick(contact)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "${contact.name} 연락처 삭제",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = offsetX.value.roundToInt(),
                        y = 0
                    )
                }
                .background(KuitTheme.colors.white)
                .pointerInput(contact.contactId) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            scope.launch {
                                offsetX.snapTo(
                                    targetValue = (
                                            offsetX.value + dragAmount
                                            ).coerceIn(
                                            minimumValue = -deleteWidthPx,
                                            maximumValue = 0f
                                        )
                                )
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                val shouldOpen =
                                    offsetX.value < -deleteWidthPx / 2f

                                offsetX.animateTo(
                                    if (shouldOpen) -deleteWidthPx else 0f
                                )

                                onOpenChange(shouldOpen)
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f)
                                onOpenChange(false)
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 기존 프로필 이미지와 이름 Row
            ContactInformation(contact = contact)

            Icon(
                painter = painterResource(R.drawable.ic_phone),
                contentDescription = "${contact.name}에게 전화",
                tint = KuitTheme.colors.gray1,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        onContactClick(contact)
                    }
            )
        }
    }
}

@Composable
private fun ContactInformation(
    contact: Contact
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
}
