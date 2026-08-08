package com.example.myapplication.ui.call.call_home

data class DeviceContact(       //기기용 연락처 모델
    val name: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)