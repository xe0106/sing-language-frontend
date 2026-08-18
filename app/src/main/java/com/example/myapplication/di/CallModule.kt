package com.example.myapplication.di

import com.example.myapplication.api.CallApiService
import com.example.myapplication.network.call.CallSocketDataSource
import com.example.myapplication.network.call.IncomingCallSocketDataSource
import com.example.myapplication.network.call.OkHttpCallSocketDataSource
import com.example.myapplication.network.call.OkHttpIncomingCallSocketDataSource
import com.example.myapplication.network.call.webrtc.WebRtcClient
import com.example.myapplication.network.call.webrtc.WebRtcClientImpl
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

    @Binds
    @Singleton
    abstract fun bindCallSocketDataSource(
        okHttpCallSocketDataSource: OkHttpCallSocketDataSource
    ): CallSocketDataSource

    @Binds
    @Singleton
    abstract fun bindIncomingCallSocketDataSource(
        implementation: OkHttpIncomingCallSocketDataSource
    ): IncomingCallSocketDataSource

    @Binds
    abstract fun bindWebRtcClient(
        implementation: WebRtcClientImpl
    ): WebRtcClient

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