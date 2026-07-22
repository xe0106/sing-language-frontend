package com.example.myapplication.ui.call

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val profileImageUrl: String?=null
)