package com.example.athlos.api

import com.example.athlos.ui.models.FoodSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EdamamApi {

    @GET("parser")
    suspend fun searchFood(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("ingr") ingredient: String
    ): FoodSearchResponse
}
