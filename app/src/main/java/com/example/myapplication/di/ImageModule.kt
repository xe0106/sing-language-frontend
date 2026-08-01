package com.example.myapplication.di

import com.example.myapplication.api.ImageApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageApiService(
        retrofit: Retrofit
    ): ImageApiService =
        retrofit.create(ImageApiService::class.java)
}