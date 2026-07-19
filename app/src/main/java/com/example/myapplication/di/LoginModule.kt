package com.example.myapplication.di

import com.example.myapplication.ui.login.LoginRepository
import com.example.myapplication.ui.login.LoginRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule{
    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository
}