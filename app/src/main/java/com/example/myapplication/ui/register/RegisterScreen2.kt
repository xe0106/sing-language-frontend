package com.example.myapplication.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.component.CommonButton
import com.example.myapplication.ui.theme.KuitTheme
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun RegisterScreen2(
    modifier: Modifier =Modifier
){
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCheck by remember {mutableStateOf("")}

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
            modifier=modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier= Modifier.height(55.dp))

            Box(
                modifier=modifier
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

            Spacer(modifier= Modifier.height(30.dp))

            CommonTextBox(
                des = "이름",
                phd = "이름을 입력하세요",
                value = name,
                onValueChange = { name = it }
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "이메일",
                phd = "이메일을 입력하세요",
                value = email,
                onValueChange = { email = it }
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "비밀번호",
                phd = "비밀번호 (6자리 이상)",
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "비밀번호 확인",
                phd = "비밀번호 재입력",
                value = passwordCheck,
                onValueChange = { passwordCheck = it },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier= Modifier.height(272.65.dp))

            CommonButton(
                onClick = {println("구현x")},
                buttonName = "가입하기"
            )

            Spacer(modifier= Modifier.height(22.43.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreen2Preview(){
    MyApplicationTheme{
        RegisterScreen2()
    }
}