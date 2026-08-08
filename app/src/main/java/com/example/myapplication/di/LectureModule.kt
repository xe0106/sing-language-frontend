package com.example.myapplication.di

import com.example.myapplication.api.LectureApiService
import com.example.myapplication.ui.lecture.LectureRepository
import com.example.myapplication.ui.lecture.LectureRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LectureModule{
    @Binds
    @Singleton
    abstract fun bindLectureRepository(
        lectureRepositoryImpl: LectureRepositoryImpl
    ): LectureRepository

    companion object {
        @Provides
        @Singleton
        fun provideLectureApiService(
            retrofit: Retrofit
        ): LectureApiService {
            return retrofit.create(
                LectureApiService::class.java
            )
        }
    }
}