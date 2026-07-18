package com.example.myapplication.ui.register

import javax.inject.Inject

class RegisterRepositoryImpl @Inject constructor(): RegisterRepository{
    override suspend fun nicknameCheck(nickname: String): Boolean {
        return nickname.isNotBlank()
    }

    override suspend fun register(name: String, email: String, password: String): Boolean {
        return name.isNotBlank()&&email.isNotBlank()&&password.isNotBlank()
    }
}