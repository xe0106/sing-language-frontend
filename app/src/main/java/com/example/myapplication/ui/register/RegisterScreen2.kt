package com.example.myapplication.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.component.CommonButton
import com.example.myapplication.ui.theme.KuitTheme
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun RegisterScreen2(
    modifier: Modifier =Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
){
    val uiState=viewModel.uiState

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        snackbarHost={
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) {
        RegisterScreen2Content(
            modifier = modifier.fillMaxSize(),
            uiState = uiState,
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
            onRegisterClick = viewModel::register
        )
    }
}

@Composable
private fun RegisterScreen2Content(
    modifier: Modifier = Modifier,
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onRegisterClick: () -> Unit
){
    Box(
        modifier=modifier.fillMaxSize()
    ){
        val scrollState = rememberScrollState()

        Image(
            painter= painterResource(id=R.drawable.register_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier= Modifier.height(55.dp))

            Box(
                modifier=Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ){
                Text(
                    text="회원가입",
                    color= KuitTheme.colors.black,
                    style= KuitTheme.typography.SB_22
                )
            }

            Spacer(modifier= Modifier.height(30.dp))

            CommonTextBox(
                des = "이름",
                phd = "이름을 입력하세요",
                value = uiState.name,
                onValueChange = onNameChange
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "이메일",
                phd = "이메일을 입력하세요",
                value = uiState.email,
                onValueChange = onEmailChange
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "비밀번호",
                phd = "비밀번호 (6자리 이상)",
                value = uiState.password,
                onValueChange = onPasswordChange,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "비밀번호 확인",
                phd = "비밀번호 재입력",
                value = uiState.passwordConfirm,
                onValueChange = onPasswordConfirmChange,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier= Modifier.height(272.65.dp))

            CommonButton(
                onClick = onRegisterClick,
                buttonName = "가입하기",
                isLoading = uiState.isLoading
            )

            Spacer(modifier= Modifier.height(22.43.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreen2Preview(){
    MyApplicationTheme{
        RegisterScreen2Content(
            uiState = RegisterUiState(),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onRegisterClick = {}
        )
    }
}