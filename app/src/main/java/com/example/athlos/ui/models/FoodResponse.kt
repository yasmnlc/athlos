package com.example.athlos.ui.models

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    @SerializedName("hints") val hints: List<Hint>
)

data class Hint(
    @SerializedName("food") val food: FoodItemApi
)

data class FoodItemApi(
    @SerializedName("label") val name: String,
    @SerializedName("nutrients") val nutrients: Nutrients
)

data class Nutrients(
    @SerializedName("ENERC_KCAL") val calories: Float?,
    @SerializedName("PROCNT") val protein: Float?,
    @SerializedName("FAT") val fat: Float?,
    @SerializedName("CHOCDF") val carbohydrate: Float?,
    @SerializedName("FIBTG") val fiber: Float?
)

data class FoodItem(
    val name: String,
    val quantity: Float = 100f,
    val unit: String = "g",
    val calories: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carbohydrate: Float = 0f,
    val fiber: Float = 0f
)

fun FoodItemApi.toFoodItem(): FoodItem = FoodItem(
    name = name,
    calories = nutrients.calories ?: 0f,
    protein = nutrients.protein ?: 0f,
    fat = nutrients.fat ?: 0f,
    carbohydrate = nutrients.carbohydrate ?: 0f,
    fiber = nutrients.fiber ?: 0f
)
