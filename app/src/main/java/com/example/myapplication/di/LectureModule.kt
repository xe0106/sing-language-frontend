package com.example.myapplication.di

import com.example.myapplication.ui.lecture.LectureRepository
import com.example.myapplication.ui.lecture.LectureRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LectureModule{
    @Binds
    @Singleton
    abstract fun bindLectureRepository(
        lectureRepositoryImpl: LectureRepositoryImpl
    ): LectureRepository
}