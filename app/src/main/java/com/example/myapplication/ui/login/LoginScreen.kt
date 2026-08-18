package com.example.myapplication.ui.login

import android.R.attr.password
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.component.CommonButton
import com.example.myapplication.ui.component.CommonTextField
import com.example.myapplication.ui.theme.KuitColors
import com.example.myapplication.ui.theme.KuitTheme
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun LoginScreen(
    modifier:Modifier= Modifier,
    viewModel: LoginViewModel= hiltViewModel(),
    onLoginSuccess:()->Unit,
    onRegisterClick:()->Unit
){

    val uiState=viewModel.uiState
    val context = LocalContext.current
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is LoginViewModel.LoginEvent.LoginResult -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    if (event.isSuccess) {
                        currentOnLoginSuccess()
                    }
                }
            }
        }
    }

    LoginScreenContent(
        modifier=modifier,
        uiState=uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick
    )
}

@Composable
private fun LoginScreenContent(
    modifier:Modifier=Modifier,
    uiState: LoginUiState,
    onEmailChange:(String)->Unit,
    onPasswordChange:(String)->Unit,
    onLoginClick:()->Unit,
    onRegisterClick:()->Unit
){
    Box(
        modifier=modifier.fillMaxSize()
    ){
        Image(
            painter= painterResource(id=R.drawable.login_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            CommonTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeHolder = "이메일",
            )

            Spacer(modifier= Modifier.height(12.dp))

            CommonTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeHolder = "비밀번호",
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier= Modifier.height(12.dp))

            Text(
                modifier=modifier
                    .width(77.dp)
                    .height(17.dp)
                    .clickable{
                        onRegisterClick()
                    },
                text="회원가입 하기",
                color= KuitTheme.colors.white,
                style= KuitTheme.typography.M_14,
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier= Modifier.height(39.dp))

            CommonButton(
                onClick = onLoginClick,
                buttonName = "로그인",
                isLoading = uiState.isLoading
            )

            Spacer(modifier= Modifier.height(22.43.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}
