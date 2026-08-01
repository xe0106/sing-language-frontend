package com.example.myapplication.ui.register

import com.example.myapplication.api.AuthApiService
import com.example.myapplication.api.ImageApiService
import com.example.myapplication.dto.RegisterRequest
import com.example.myapplication.network.ImagePartFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class RegisterRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val imageApiService: ImageApiService,
    private val imagePartFactory: ImagePartFactory
): RegisterRepository{

    override suspend fun uploadProfileImage(
        profileImageUri: String
    ): String? = withContext(Dispatchers.IO){
        try {
            val imagePart = imagePartFactory.create(
                imageUri = profileImageUri
            ) ?: return@withContext null

            val response = imageApiService.uploadImage(
                file = imagePart
            )

            val responseBody = response.body()

            if (
                response.isSuccessful &&
                responseBody?.isSuccess == true
            ) {
                responseBody.data?.imageUrl
            } else {
                null
            }
        } catch (exception: IOException) {
            null
        } catch (exception: SecurityException) {
            null
        } catch (exception: IllegalArgumentException) {
            null
        }
    }

    override suspend fun nicknameCheck(
        nickname: String
    ): Boolean {
        //return nickname.isNotBlank()

        val trimmedNickname=nickname.trim()

        if(trimmedNickname.isBlank()) {
            return false
        }

        return try {
            val response = authApiService.checkNickname(
                nickname=trimmedNickname
            )

            response.isSuccessful &&
                    response.body()?.isSuccess ==true
        } catch (exception: IOException) {
            false
        } catch (exception: Exception) {
            false
        }
    }

    override suspend fun register(
         email: String,
         password: String,
         name: String,
         profileImageUrl: String,
         nickname: String,
         gender: String,
         birthDate: String,
         phoneNumber: String
    ): Boolean {
        //return name.isNotBlank()&&email.isNotBlank()&&password.isNotBlank()

        return try {
            val response = authApiService.register(
                RegisterRequest(
                    email = email,
                    password=password,
                    name=name,
                    profileImageUrl=profileImageUrl,
                    nickname=nickname,
                    gender=gender,
                    birthDate=birthDate,
                    phoneNumber=phoneNumber
                )
            )

            response.isSuccessful &&
                    response.body()?.isSuccess ==true
        } catch (exception: IOException) {
            false
        } catch (exception: Exception) {
            false
        }
    }
}