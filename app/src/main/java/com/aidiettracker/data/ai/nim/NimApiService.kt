package com.aidiettracker.data.ai.nim

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NimApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("v1/chat/completions")
    fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: NimChatRequest
    ): Call<NimChatResponse>
}
