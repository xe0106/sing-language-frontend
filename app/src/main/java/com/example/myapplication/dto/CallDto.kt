package com.example.myapplication.dto

data class ContactResponse(
    val contactId: Long,
    val targetUserId: Long,
    val contactName: String,
    val profileImageUrl: String,
    val createdAt: String,
    val lastContactedAt: String
)

data class ContactInsertRequest(
    val phoneNumber: String,
    val contactName: String,
    val profileImageUrl: String? = null
)

data class ContactInsertResponse(
    val contactId: Long,
    val userId: Long,
    val targetUserId: Long,
    val contactName: String,
    val createdAt: String
)