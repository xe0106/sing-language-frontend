package com.example.myapplication.di

import com.example.myapplication.ui.quiz.QuizRepository
import com.example.myapplication.ui.quiz.QuizRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class QuizModule {

    @Binds
    abstract fun bindQuizRepository(
        impl: QuizRepositoryImpl
    ): QuizRepository
}