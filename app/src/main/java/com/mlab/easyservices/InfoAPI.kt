package com.mlab.easyservices

import retrofit2.http.GET

interface InfoAPI {
    companion object{
        const val BASE_URL="https://charlesleggett79.github.io"
    }

    @GET("/link/link.json")
    suspend fun getWebInfo() : WebInfo
}