package com.example.myapplication.ui.call.call_home

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceContactReader @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    fun readContact(uri: Uri): Contact {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        ).use {cursor->
            if (cursor == null || !cursor.moveToFirst()) {
                throw IllegalStateException("연락처를 읽을 수 없습니다.")
            }

            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
            )

            val phoneNumber = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
            )

            val profileImageUrl = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                )
            )

            return Contact(
                id = 0L,
                name = name,
                phoneNumber = phoneNumber,
                profileImageUrl = profileImageUrl
            )
        }
    }
}