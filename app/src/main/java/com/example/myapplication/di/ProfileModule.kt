package com.example.myapplication.di

import com.example.myapplication.ui.mypage.ProfileApiService
import com.example.myapplication.ui.mypage.ProfileRepository
import com.example.myapplication.ui.mypage.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    companion object {

        @Provides
        @Singleton
        fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
            retrofit.create(ProfileApiService::class.java)
    }
}