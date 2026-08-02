package com.example.myapplication.api

import com.example.myapplication.dto.ImageUploadResult
import com.example.myapplication.network.ApiResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApiService {

    @Multipart
    @POST("sign/language/images/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ImageUploadResult>>
}