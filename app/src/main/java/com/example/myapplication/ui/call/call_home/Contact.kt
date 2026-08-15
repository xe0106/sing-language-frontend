package com.example.myapplication.ui.call.call_home

data class Contact(     //서버용 연락체 모델
    val contactId: Long,
    val targetUserId: Long,
    val name: String,
    val profileImageUrl: String?=null
)