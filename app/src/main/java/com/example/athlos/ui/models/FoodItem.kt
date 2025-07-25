package com.example.athlos.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "food_entries")
data class FoodItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    // Alterado para permitir valores nulos
    val date: String? = null,
    val mealType: String? = null,
    val name: String,
    val grams: Int,
    val baseCalories: Int,
    val baseProtein: Double,
    val baseCarbohydrate: Double,
    val baseFat: Double,
    val baseFiber: Double
) {
    val calories: Double
        get() = (baseCalories / 100.0) * grams
    val protein: Double
        get() = (baseProtein / 100.0) * grams
    val carbohydrate: Double
        get() = (baseCarbohydrate / 100.0) * grams
    val fat: Double
        get() = (baseFat / 100.0) * grams
    val fiber: Double
        get() = (baseFiber / 100.0) * grams
}