package com.example.myapplication.ui.login

import com.example.myapplication.api.AuthApiService
import com.example.myapplication.dto.LoginRequest
import com.example.myapplication.network.SessionManager
import java.io.IOException
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : LoginRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Boolean {

        //return email.isNotBlank() && password.isNotBlank()

        return try{
            val response = authApiService.login(
                LoginRequest(
                    email = email,
                    password=password
                )
            )

            val body = response.body()
            val result=body?.data

            if(
                response.isSuccessful &&
                body?.isSuccess ==true &&
                result != null
            ){
                sessionManager.updateTokens(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken
                )

                true
            } else {
                false
            }
        } catch (exception: IOException) {
            false
        } catch (exception: Exception) {
            false
        }
    }
}