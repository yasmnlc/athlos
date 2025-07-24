package com.example.athlos.ui.models

import com.google.gson.annotations.SerializedName
import com.example.athlos.ui.models.FoodItem


data class FoodSearchResponse(
    @SerializedName("foods") val foods: FoodsContainer
)

data class FoodsContainer(
    @SerializedName("food") val food: List<FatSecretFood>
)

data class FatSecretFood(
    @SerializedName("food_id") val id: String,
    @SerializedName("food_name") val name: String,
    @SerializedName("food_description") val description: String
)


fun FatSecretFood.toFoodItem(): FoodItem {
    val desc = description
    return FoodItem(
        name = name
    )
}
