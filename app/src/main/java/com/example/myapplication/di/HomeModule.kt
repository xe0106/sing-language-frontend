package com.example.myapplication.di

import com.example.myapplication.ui.home.HomeRepository
import com.example.myapplication.ui.home.HomeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    abstract fun bindHomeRepository(
        impl: HomeRepositoryImpl
    ): HomeRepository
}