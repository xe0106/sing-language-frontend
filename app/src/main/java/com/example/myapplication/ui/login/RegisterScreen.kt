package com.example.myapplication.ui.login

import android.R.attr.name
import android.R.attr.onClick
import android.R.attr.password
import android.R.attr.text
import android.R.attr.textColor
import android.R.attr.textStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.component.CommonButton
import com.example.myapplication.ui.component.CommonTextField
import com.example.myapplication.ui.theme.KuitTheme

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
                value = name
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "이메일",
                phd = "이메일을 입력하세요",
                value = email
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "비밀번호",
                phd = "비밀번호 (6자리 이상)",
                value = password
            )

            Spacer(modifier= Modifier.height(20.dp))

            CommonTextBox(
                des = "비밀번호 확인",
                phd = "비밀번호 재입력",
                value = passwordCheck
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

@Composable
fun RegisterScreen1(
    modifier: Modifier =Modifier
){
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var birthDate by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

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

            Spacer(modifier= Modifier.height(19.dp))

            Box(
                modifier=modifier
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
                modifier=modifier
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
                modifier=modifier
                    .width(80.dp)
                    .height(80.dp)
            ){
                Image(
                    painter= painterResource(id=R.drawable.basic_profile),
                    contentDescription = null,
                    modifier= Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Image(
                    painter= painterResource(id=R.drawable.ellipse_196),
                    contentDescription = null,
                    modifier= Modifier
                        .align(Alignment.TopEnd)
                        .width(20.dp)
                        .height(20.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier= Modifier.height(19.dp))

            Text(
                modifier=modifier
                    .width(345.dp)
                    .height(20.dp),
                text="닉네임",
                color=Color(0XFF364153),
                style= KuitTheme.typography.R_14
            )

            Spacer(modifier= Modifier.height(4.dp))

            Box(
                modifier=modifier
                    .width(345.dp)
                    .height(50.dp)
            ){
                CommonTextField(
                    placeHolder = "닉네임",
                    textValue = nickname
                )

                Button(
                    onClick = {println("not found")},
                    modifier = modifier
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
                        style = KuitTheme.typography.M_12,
                        color = Color(0XFF999999)
                    )
                }
            }

            Spacer(modifier= Modifier.height(20.dp))

            Text(
                modifier=modifier
                    .width(345.dp)
                    .height(20.dp),
                text="성별",
                color=Color(0XFF364153),
                style= KuitTheme.typography.R_14
            )

            Spacer(modifier= Modifier.height(4.dp))

            GenderToggleButton(
                selectedGender = gender,
                onGenderSelected = {gender=it}
            )

            Spacer(modifier= Modifier.height(21.93.dp))

            CommonTextBox(
                des="생년월일",
                phd="사용자 입력",
                value = birthDate
            )

            Spacer(modifier= Modifier.height(20.11.dp))

            CommonTextBox(
                des="전화번호",
                phd="사용자 입력",
                value = phoneNumber
            )

            Spacer(modifier= Modifier.height(102.04.dp))

            CommonButton(
                onClick = {println("구현x")},
                buttonName = "시작하기"
            )

            Spacer(modifier= Modifier.height(22.43.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreen1Preview(){
    RegisterScreen2()
}