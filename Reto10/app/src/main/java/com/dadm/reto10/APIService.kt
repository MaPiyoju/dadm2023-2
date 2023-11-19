package com.dadm.reto10

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface APIService {
    @GET
    suspend fun getData(@Url url:String): Response<OpenDataResponse>
}