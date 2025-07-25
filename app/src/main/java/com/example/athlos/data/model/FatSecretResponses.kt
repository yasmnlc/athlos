package com.example.athlos.model

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("token_type") val tokenType: String
)

data class FoodSearchResponse(
    @SerializedName("foods") val foods: FoodListContainer
)

data class FoodListContainer(
    @SerializedName("food") val food: List<FatSecretFood>
)

data class FatSecretFood(
    @SerializedName("food_id") val id: String,
    @SerializedName("food_name") val name: String,
    @SerializedName("food_type") val type: String,
    @SerializedName("brand_name") val brand: String?,
    @SerializedName("food_description") val description: String
)
