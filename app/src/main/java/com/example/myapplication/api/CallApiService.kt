package com.example.myapplication.api

import com.example.myapplication.dto.CallOutRequest
import com.example.myapplication.dto.CallOutResponse
import com.example.myapplication.dto.ContactInsertRequest
import com.example.myapplication.dto.ContactInsertResponse
import com.example.myapplication.dto.ContactResponse
import com.example.myapplication.dto.SubtitleResponse
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CallApiService {
    //연락처 목록 조회 api
    @GET("sign/language/contacts")
    suspend fun viewContactList()
    : Response<ApiResponse<List<ContactResponse>>>

    //연락처 추가 api
    @POST("sign/language/contacts/insert")
    suspend fun insertContact(
        @Body request: ContactInsertRequest
    ): Response<ApiResponse<ContactInsertResponse>>

    //연락처 삭제 api
    @DELETE("sign/language/contacts/delete/{targetUserId}")
    suspend fun deleteContact(
        @Path("targetUserId") targetUserId: Long
    ): Response<ApiResponse<Unit>>

    //전화 발신 api
    @POST("sign/language/call")
    suspend fun callOut(
        @Body request: CallOutRequest
    ): Response<ApiResponse<CallOutResponse>>

    //자막 목록 조회 api
    @GET("sign/language/call/{callId}/subtitles/list")
    suspend fun viewSubtitleList(
        @Path("callId") callId: String
    ): Response<ApiResponse<List<SubtitleResponse>>>
}