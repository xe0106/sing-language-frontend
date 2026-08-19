package com.example.myapplication.ui.mypage

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun ProfileEditScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onUpdateSuccess: () -> Unit = onBackClick
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onProfileImageChange(uri?.toString())
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            Toast.makeText(context, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            viewModel.consumeUpdateSuccess()
            onUpdateSuccess()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        ProfileEditContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onBackClick = onBackClick,
            onProfileImageClick = { imagePickerLauncher.launch("image/*") },
            onNicknameChange = viewModel::onNicknameChange,
            onNicknameCheckClick = viewModel::checkNickname,
            onGenderChange = viewModel::onGenderChange,
            onBirthDateChange = viewModel::onBirthDateChange,
            onPhoneNumberChange = viewModel::onPhoneNumberChange,
            onUpdateClick = viewModel::updateProfile
        )
    }
}

@Composable
private fun ProfileEditContent(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onProfileImageClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNicknameCheckClick: () -> Unit,
    onGenderChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onUpdateClick: () -> Unit
) {
    val interactionEnabled =
        uiState.profile != null && !uiState.isLoading && !uiState.isSaving

    Column(
        modifier = modifier
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
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
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
                modifier = Modifier.clickable(
                    enabled = !uiState.isSaving,
                    onClick = onBackClick
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(80.dp)
        ) {
            AsyncImage(
                model = uiState.profileImageUri ?: uiState.profileImageUrl,
                contentDescription = "프로필 이미지",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.character_profile),
                error = painterResource(id = R.drawable.character_profile),
                fallback = painterResource(id = R.drawable.character_profile),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
            )
            Image(
                painter = painterResource(id = R.drawable.ic_pen),
                contentDescription = "프로필 사진 수정",
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(
                        enabled = interactionEnabled,
                        onClick = onProfileImageClick
                    )
                    .padding(2.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterHorizontally),
                color = KuitTheme.colors.main1,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(32.dp))
            return@Column
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProfileNicknameField(
                value = uiState.nickname,
                enabled = interactionEnabled,
                isChecking = uiState.isCheckingNickname,
                isChecked = uiState.isNicknameChecked,
                onValueChange = onNicknameChange,
                onCheckClick = onNicknameCheckClick
            )

            ProfileGenderField(
                gender = uiState.gender,
                enabled = interactionEnabled,
                onGenderChange = onGenderChange
            )

            ProfileEditField(
                label = "생년월일",
                value = uiState.birthDate,
                placeholder = "2005-01-01",
                enabled = interactionEnabled,
                onValueChange = onBirthDateChange
            )

            ProfileEditField(
                label = "전화번호",
                value = uiState.phoneNumber,
                placeholder = "전화번호를 입력해 주세요",
                enabled = interactionEnabled,
                onValueChange = onPhoneNumberChange
            )

            Button(
                onClick = onUpdateClick,
                enabled = interactionEnabled && !uiState.isCheckingNickname,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KuitTheme.colors.black,
                    contentColor = Color.White,
                    disabledContainerColor = KuitTheme.colors.black.copy(alpha = 0.5f),
                    disabledContentColor = Color.White
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "수정하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileNicknameField(
    value: String,
    enabled: Boolean,
    isChecking: Boolean,
    isChecked: Boolean,
    onValueChange: (String) -> Unit,
    onCheckClick: () -> Unit
) {
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
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = TextStyle(
                    color = KuitTheme.colors.black,
                    fontSize = 14.sp
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = when {
                    isChecking -> "확인 중"
                    isChecked -> "확인완료"
                    else -> "중복확인"
                },
                color = if (isChecked) Color(0xFF2E7D32) else Color(0xFF777777),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(
                    enabled = enabled && !isChecking,
                    onClick = onCheckClick
                )
            )
        }
    }
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    placeholder: String,
    enabled: Boolean,
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
                enabled = enabled,
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
private fun ProfileGenderField(
    gender: String,
    enabled: Boolean,
    onGenderChange: (String) -> Unit
) {
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
            ProfileGenderButton(
                text = "남성",
                selected = gender == "MALE",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = { onGenderChange("MALE") }
            )
            ProfileGenderButton(
                text = "여성",
                selected = gender == "FEMALE",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = { onGenderChange("FEMALE") }
            )
        }
    }
}

@Composable
private fun ProfileGenderButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
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
private fun ProfileEditScreenPreview() {
    ProfileEditContent(
        uiState = ProfileUiState(
            nickname = "사용자",
            originalNickname = "사용자",
            phoneNumber = "010-1234-5678",
            originalPhoneNumber = "010-1234-5678",
            gender = "MALE",
            originalGender = "MALE",
            birthDate = "2000-01-01",
            originalBirthDate = "2000-01-01",
            isNicknameChecked = true
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNicknameChange = {},
        onNicknameCheckClick = {},
        onGenderChange = {},
        onBirthDateChange = {},
        onPhoneNumberChange = {},
        onUpdateClick = {}
    )
}
