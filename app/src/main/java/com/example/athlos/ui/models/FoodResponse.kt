package com.example.athlos.ui.models

import com.google.gson.annotations.SerializedName

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
    val desc = this.description

    // auxiliar para extrair um valor numérico da string de descrição
    fun extractValue(pattern: String): Double {
        return Regex(pattern).find(desc)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    // extrai cada valor nutricional usando a função auxiliar
    val calories = extractValue("""Calories: (\d+\.?\d*)kcal""").toInt()
    val fat = extractValue("""Fat: (\d+\.?\d*)g""")
    val carbs = extractValue("""Carbs: (\d+\.?\d*)g""")
    val protein = extractValue("""Protein: (\d+\.?\d*)g""")

    // retorna o FoodItem com todos os campos obrigatórios preenchidos
    return FoodItem(
        id = this.id,
        name = this.name,
        grams = 100,
        baseCalories = calories,
        baseFat = fat,
        baseCarbohydrate = carbs,
        baseProtein = protein,
        baseFiber = 0.0,
        date = null,
        mealType = null
    )
}