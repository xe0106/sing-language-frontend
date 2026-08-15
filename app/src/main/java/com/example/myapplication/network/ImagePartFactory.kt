package com.example.myapplication.network

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagePartFactory @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun create(
        imageUri: String
    ): MultipartBody.Part? {
        val uri = Uri.parse(imageUri)
        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(uri)
            ?: "image/jpeg"

        val imageBytes = contentResolver
            .openInputStream(uri)
            ?.use {inputStream ->
                inputStream.readBytes()
            }
            ?: return null

        val requestBody = imageBytes.toRequestBody(
            mimeType.toMediaType()
        )

        return MultipartBody.Part.createFormData(
            name = FILE_PART_NAME,
            filename = DEFAULT_FILE_NAME,
            body = requestBody
        )
    }

    fun createFromResource(
        @DrawableRes imageResourceId: Int
    ): MultipartBody.Part {
        val imageBytes = context.resources
            .openRawResource(imageResourceId)
            .use { inputStream ->
                inputStream.readBytes()
            }

        val requestBody = imageBytes.toRequestBody(
            "image/png".toMediaType()
        )

        return MultipartBody.Part.createFormData(
            name = FILE_PART_NAME,
            filename = DEFAULT_PROFILE_FILE_NAME,
            body = requestBody
        )
    }

    private companion object{
        const val FILE_PART_NAME = "file"
        const val DEFAULT_FILE_NAME = "profile.jpg"
        const val DEFAULT_PROFILE_FILE_NAME = "default_profile.png"
    }
}