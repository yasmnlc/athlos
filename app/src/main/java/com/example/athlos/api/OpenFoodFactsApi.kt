package com.example.athlos.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class OpenFoodFactsSearchResponse(
    val products: List<OpenFoodProduct>
)

data class OpenFoodProduct(
    val product_name: String?,
    val nutriments: Nutriments?
)

data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Float?,

    @SerializedName("proteins_100g")
    val proteins_100g: Float?,

    @SerializedName("carbohydrates_100g")
    val carbohydrates_100g: Float?,

    @SerializedName("fat_100g")
    val fat_100g: Float?,

    @SerializedName("fiber_100g")
    val fiber_100g: Float?
)

interface OpenFoodFactsApi {
    @GET("cgi/search.pl?search_simple=1&action=process&json=1&lc=pt")
    suspend fun searchFoods(
        @Query("search_terms") searchTerms: String
    ): OpenFoodFactsSearchResponse
}