package com.example.myapplication.ui.login

import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor() : LoginRepository {
    override suspend fun login(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }
}