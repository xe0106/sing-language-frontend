package com.example.myapplication.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 모든 요청에 "Authorization: Bearer {accessToken}" 헤더를 붙여주는 인터셉터.
 * 토큰이 없으면 헤더 없이 그대로 요청한다. (로그인/회원가입 등)
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (path in PUBLIC_AUTH_PATHS) {
            return chain.proceed(
                originalRequest.newBuilder()
                    .removeHeader("Authorization")
                    .build()
            )
        }

        val token = sessionManager.accessToken

        val request = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(request)
    }

    private companion object {
        val PUBLIC_AUTH_PATHS = setOf(
            "/sign/language/auth/signin",
            "/sign/language/auth/signup",
            "/sign/language/auth/check-nickname"
        )
    }
}
