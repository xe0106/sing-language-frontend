package com.example.myapplication.network

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 세션 정보(액세스 토큰, userId)를 보관하는 클래스.
 *
 * TODO(로그인 파트): 로그인 성공 시 아래 값을 반드시 저장해야 함
 *   - sessionManager.update(accessToken = ..., userId = ...)
 * TODO: 앱 재시작 후에도 로그인 유지 필요하면 DataStore 등으로 교체
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

    fun updateTokens(
        accessToken: String,
        refreshToken: String
    ){
        this.accessToken=accessToken
        this.refreshToken=refreshToken
    }

    fun update(accessToken: String?, userId: Long?) {
        this.accessToken = accessToken
        this.userId = userId
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        userId = null
    }
}