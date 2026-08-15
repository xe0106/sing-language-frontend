package com.example.myapplication.network

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 세션 정보(access token, refresh token, userId)를 메모리에 보관한다.
 *
 * 로그인 성공 시 [updateSession]을 호출해 세 값을 함께 저장한다.
 * 토큰을 재발급받은 경우에는 [updateTokens]로 토큰만 갱신하고 기존 userId를 유지한다.
 *
 * 앱 재시작 후에도 로그인을 유지해야 한다면 DataStore 등을 사용하는 방식으로 교체해야 한다.
 */
@Singleton
class SessionManager @Inject constructor() {

    @Volatile
    var accessToken: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    @Volatile
    var userId: Long? = null
        private set

    //로그인 성공시 호출
    fun updateSession(
        accessToken: String,
        refreshToken: String,
        userId: Long
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userId = userId
    }

    //토큰 재발급시 호출, userId는 기존 값 유지
    fun updateTokens(
        accessToken: String,
        refreshToken: String
    ){
        this.accessToken=accessToken
        this.refreshToken=refreshToken
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        userId = null
    }
}
