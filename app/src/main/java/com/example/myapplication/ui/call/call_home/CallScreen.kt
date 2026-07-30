package com.example.myapplication.ui.call.call_home

import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallScreen(
    modifier: Modifier= Modifier,
    viewModel: CallViewModel = hiltViewModel(),
    onSettingsClick:()->Unit,
    onContactClick:(Contact)->Unit
){
    val uiState=viewModel.uiState

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result->
        val contactUri = result.data?.data
        if(contactUri !=null){
            viewModel.addContactFromDevice(contactUri)
        }
    }

    Box(
        modifier=modifier.fillMaxSize()
    ){
        Image(
            painter= painterResource(id=R.drawable.lecture_background),
            contentDescription = null,
            modifier= Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier= Modifier.height(50.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        text = "연속학습 12",
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

            Spacer(modifier= Modifier.height(18.dp))

            ContactBox(
                modifier = Modifier.padding(horizontal = 16.dp),
                contacts = uiState.contacts,
                onContactClick = onContactClick
            )
        }

        FloatingActionButton(
            onClick = {
                val intent = Intent(Intent.ACTION_PICK).apply{
                    type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                }
                pickContactLauncher.launch(intent)
            },
            modifier= Modifier
                .align(Alignment.BottomEnd)
                .padding(end=24.dp, bottom = 140.dp),
            containerColor = KuitTheme.colors.main1,
            contentColor = KuitTheme.colors.white
        ) {
            Text(
                text = "+",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}