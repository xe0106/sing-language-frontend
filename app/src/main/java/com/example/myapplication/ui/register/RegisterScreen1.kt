package com.example.myapplication.ui.register

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.component.CommonButton
import com.example.myapplication.ui.component.CommonTextField
import com.example.myapplication.ui.theme.KuitTheme
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun RegisterScreen1(
    modifier: Modifier =Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
){
    val uiState=viewModel.uiState

    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onProfileImageChange(uri?.toString())
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost={
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) {
        RegisterScreen1Content(
            modifier = modifier.fillMaxSize(),
            uiState = uiState,
            onProfileImageClick = {
                imagePickerLauncher.launch("image/*")
            },
            onNicknameChange = viewModel::onNicknameChange,
            onNicknameCheckClick = viewModel::nicknameCheck,
            onGenderChange = viewModel::onGenderChange,
            onBirthChange = viewModel::onBirthChange,
            onPhoneNumberChange = viewModel::onPhoneNumberChange,
            onNextClick = onNextClick
        )
    }
}

@Composable
private fun RegisterScreen1Content(
    modifier: Modifier = Modifier,
    uiState: RegisterUiState,
    onProfileImageClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNicknameCheckClick: () -> Unit,
    onGenderChange: (Gender) -> Unit,
    onBirthChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onNextClick: () -> Unit
){
    Box(
        modifier=modifier.fillMaxSize()
    ){

        Image(
            painter= painterResource(id=R.drawable.register_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier= Modifier.height(55.dp))

            Box(
                modifier=Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp, vertical = 15.dp)
            ){
                Text(
                    text="회원가입",
                    color= KuitTheme.colors.black,
                    style= KuitTheme.typography.SB_22
                )
            }

            Spacer(modifier= Modifier.height(19.dp))

            Box(
                modifier=Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 20.dp)
            ){
                Text(
                    text="안녕하세요! 사용자님을 소개해주세요",
                    color= Color(0XFF1A1A1A),
                    style= KuitTheme.typography.SB_20
                )
            }

            Spacer(modifier= Modifier.height(6.dp))

            Box(
                modifier=Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 20.dp)
            ){
                Text(
                    text="언제든 수정 가능해요!",
                    color= KuitTheme.colors.gray2,
                    style= KuitTheme.typography.M_14
                )
            }

            Spacer(modifier= Modifier.height(37.dp))

            Box(
                modifier=Modifier
                    .width(80.dp)
                    .height(80.dp)
            ){
                AsyncImage(
                    model = uiState.profileImageUri ?: R.drawable.basic_profile2,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Image(
                    painter= painterResource(id=R.drawable.ellipse_196),
                    contentDescription = null,
                    modifier= Modifier
                        .align(Alignment.TopEnd)
                        .width(20.dp)
                        .height(20.dp)
                        .clickable{
                            onProfileImageClick()
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier= Modifier.height(19.dp))

            Text(
                modifier=Modifier
                    .width(345.dp)
                    .height(20.dp),
                text="닉네임",
                color=Color(0XFF364153),
                style= KuitTheme.typography.R_14
            )

            Spacer(modifier= Modifier.height(4.dp))

            Box(
                modifier=Modifier
                    .width(345.dp)
                    .height(50.dp)
            ){
                CommonTextField(
                    value = uiState.nickname,
                    onValueChange = onNicknameChange,
                    placeHolder = "닉네임",
                )

                Button(
                    onClick = onNicknameCheckClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x=(-18).dp)
                        .width(56.dp)
                        .height(21.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0XFFF1F3F5)
                    ),
                    shape = RoundedCornerShape(10.5.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "중복확인",
                        style = KuitTheme.typography.R_12,
                        color = Color(0XFF999999)
                    )
                }
            }

            Spacer(modifier= Modifier.height(20.dp))

            Text(
                modifier=Modifier
                    .width(345.dp)
                    .height(20.dp),
                text="성별",
                color=Color(0XFF364153),
                style= KuitTheme.typography.R_14
            )

            Spacer(modifier= Modifier.height(4.dp))

            GenderToggleButton(
                selectedGender = uiState.gender,
                onGenderSelected = onGenderChange
            )

            Spacer(modifier= Modifier.height(21.93.dp))

            CommonTextBox(
                des = "생년월일",
                phd = "ex) 2005-01-01",
                value = uiState.birth,
                onValueChange = onBirthChange
            )

            Spacer(modifier= Modifier.height(20.11.dp))

            CommonTextBox(
                des = "전화번호",
                phd = "ex) 01067890123",
                value = uiState.phoneNumber,
                onValueChange = onPhoneNumberChange
            )

            Spacer(modifier= Modifier.height(102.04.dp))

            CommonButton(
                onClick = onNextClick,
                buttonName = "시작하기"
            )

            Spacer(modifier= Modifier.height(22.43.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreen1Preview(){
    MyApplicationTheme{
        RegisterScreen1Content(
            uiState = RegisterUiState(),
            onProfileImageClick = {},
            onNicknameChange = {},
            onNicknameCheckClick = {},
            onGenderChange = {},
            onBirthChange = {},
            onPhoneNumberChange = {},
            onNextClick = {}
        )
    }
}
