package com.example.athlos.ui.models

import java.util.UUID

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Desconhecido",

    // Valores nutricionais base, sempre referentes a 100g
    val baseCalories: Int = 0,
    val baseProtein: Double = 0.0,
    val baseCarbohydrate: Double = 0.0,
    val baseFat: Double = 0.0,
    val baseFiber: Double = 0.0,

    // Quantidade em gramas que o usuário consumiu (pode ser editada)
    var grams: Int = 100
) {
    // Propriedades calculadas dinamicamente com base nos gramas
    val calories: Int
        get() = (baseCalories / 100.0 * grams).toInt()
    val protein: Double
        get() = baseProtein / 100.0 * grams
    val carbohydrate: Double
        get() = baseCarbohydrate / 100.0 * grams
    val fat: Double
        get() = baseFat / 100.0 * grams
    val fiber: Double
        get() = baseFiber / 100.0 * grams
}