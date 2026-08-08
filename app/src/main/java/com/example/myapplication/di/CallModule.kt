package com.example.myapplication.di

import com.example.myapplication.api.CallApiService
import com.example.myapplication.ui.call.CallRepository
import com.example.myapplication.ui.call.CallRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallModule{
    @Binds
    @Singleton
    abstract fun bindCallRepository(
        callRepositoryImpl: CallRepositoryImpl
    ): CallRepository

    companion object {
        @Provides
        @Singleton
        fun provideCallApiService(
            retrofit: Retrofit
        ) : CallApiService {
            return retrofit.create(
                CallApiService::class.java
            )
        }
    }
}