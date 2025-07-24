package com.example.athlos.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExerciseDbService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://exercisedb.p.rapidapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ExerciseDbApi = retrofit.create(ExerciseDbApi::class.java)
}