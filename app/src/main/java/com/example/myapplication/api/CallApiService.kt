package com.example.myapplication.api

import com.example.myapplication.dto.ContactInsertRequest
import com.example.myapplication.dto.ContactInsertResponse
import com.example.myapplication.dto.ContactResponse
import com.example.myapplication.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CallApiService {
    @GET("sign/language/contacts")
    suspend fun viewContactList()
    : Response<ApiResponse<List<ContactResponse>>>

    @POST("sign/language/contacts/insert")
    suspend fun insertContact(
        @Body request: ContactInsertRequest
    ): Response<ApiResponse<ContactInsertResponse>>

    @DELETE("sign/language/contacts/delete/{targetUserId}")
    suspend fun deleteContact(
        @Path("targetUserId") targetUserId: Long
    ): Response<ApiResponse<Unit>>
}