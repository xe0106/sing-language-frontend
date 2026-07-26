package com.example.myapplication.ui.call.call_home

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val profileImageUrl: String?=null
)