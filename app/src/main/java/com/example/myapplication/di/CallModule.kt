package com.example.myapplication.di

import com.example.myapplication.ui.call.CallRepository
import com.example.myapplication.ui.call.CallRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallModule{
    @Binds
    @Singleton
    abstract fun bindCallRepository(
        callRepositoryImpl: CallRepositoryImpl
    ): CallRepository
}