package com.example.retrofitweb.interfaces

import com.example.retrofitweb.data.models.UVIndexData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface RetrofitService {
    @GET("get_all_client.php")
    suspend fun getUVIndexData(): List<UVIndexData>
}

object RetrofitBuilder {
    private const val BASE_URL = "http://gitea.espoch.edu.ec:8085/infosolar/api-rest/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getRetrofitService(): RetrofitService {
        return retrofit.create(RetrofitService::class.java)
    }
}

