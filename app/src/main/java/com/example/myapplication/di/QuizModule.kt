package com.example.myapplication.di

import com.example.myapplication.ui.quiz.QuizApiService
import com.example.myapplication.ui.quiz.QuizRepository
import com.example.myapplication.ui.quiz.QuizRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuizModule {

    @Binds
    abstract fun bindQuizRepository(
        impl: QuizRepositoryImpl
    ): QuizRepository

    companion object {
        @Provides
        @Singleton
        fun provideQuizApiService(retrofit: Retrofit): QuizApiService =
            retrofit.create(QuizApiService::class.java)
    }
}