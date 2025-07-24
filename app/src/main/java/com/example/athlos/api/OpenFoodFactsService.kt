package com.example.athlos.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenFoodFactsService {
    val api: OpenFoodFactsApi = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenFoodFactsApi::class.java)
}
