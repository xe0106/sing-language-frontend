package com.example.myapplication.di

import com.example.myapplication.ui.register.RegisterRepository
import com.example.myapplication.ui.register.RegisterRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RegisterModule {
    @Binds
    @Singleton
    abstract fun bindRegisterRepository(
        registerRepositoryImpl: RegisterRepositoryImpl
    ): RegisterRepository
}