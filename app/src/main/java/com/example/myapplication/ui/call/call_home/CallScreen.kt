package com.example.myapplication.ui.call.call_home

import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.call.callMediaPermissions
import com.example.myapplication.ui.call.hasCallMediaPermissions
import com.example.myapplication.ui.theme.KuitTheme

@Composable
fun CallScreen(
    modifier: Modifier= Modifier,
    viewModel: CallViewModel = hiltViewModel(),
    onSettingsClick:()->Unit,
    onCallStarted: (String)-> Unit
){
    val uiState=viewModel.uiState

    val context = LocalContext.current

    var pendingCallContact by remember {
        mutableStateOf<Contact?>(null)
    }

    val mediaPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) {
            val contact = pendingCallContact
            pendingCallContact = null

            if (
                context.hasCallMediaPermissions() &&
                contact != null
            ) {
                viewModel.startCall(contact)
            } else {
                Toast.makeText(
                    context,
                    "영상통화를 위해 카메라와 마이크 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result->
        val contactUri = result.data?.data
        if(contactUri !=null){
            viewModel.addContactFromDevice(contactUri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CallViewModel.CallEvent.NavigateToVideoCall -> {
                    onCallStarted(event.callId)
                }
            }
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "연속학습 ${uiState.learningDays}",
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

            Spacer(modifier = Modifier.height(12.dp))

            ContactBox(
                modifier = Modifier
                    .weight(weight = 1f, fill = false),
                contacts = uiState.contacts,
                onContactClick = { contact ->
                    if (context.hasCallMediaPermissions()) {
                        viewModel.startCall(contact)
                    } else {
                        pendingCallContact = contact

                        mediaPermissionLauncher.launch(
                            callMediaPermissions
                        )
                    }
                },
                onDeleteContact = viewModel::deleteContact,
                onAddContactClick = {
                    val intent = Intent(Intent.ACTION_PICK).apply{
                        type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    }
                    pickContactLauncher.launch(intent)
                }
            )

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
