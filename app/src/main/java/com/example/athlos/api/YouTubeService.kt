package com.example.athlos.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object YouTubeService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: YouTubeApi = retrofit.create(YouTubeApi::class.java)
}