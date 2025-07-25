package com.example.athlos.utils

object NutritionalCalculator {
    enum class ActivityLevel(val description: String, val factor: Double) {
        SEDENTARY("Sedentário (Pouco ou nenhum exercício)", 1.2),
        LIGHTLY_ACTIVE("Levemente ativo (Exercício leve 1–3 dias/semana)", 1.375),
        MODERATELY_ACTIVE("Moderadamente ativo (Exercício moderado 3–5 dias/semana)", 1.55),
        VERY_ACTIVE("Muito ativo (Exercício intenso 6–7 dias/semana)", 1.725),
        EXTREMELY_ACTIVE("Extremamente ativo (Atleta/Exercício pesado + trabalho físico)", 1.9);

        companion object {
            fun fromDescription(description: String): ActivityLevel? {
                return entries.find { it.description == description }
            }
        }
    }

    /**
     * Calcula a Taxa Metabólica Basal (TMB) usando a equação de Mifflin-St Jeor.
     */
    fun calculateBMR(pesoKg: Float, alturaCm: Float, idade: Int, sexo: String): Double {
        return if (sexo.equals("Masculino", ignoreCase = true)) {
            (10 * pesoKg) + (6.25 * alturaCm) - (5 * idade) + 5
        } else { // Assume Feminino ou Outro para a fórmula feminina como padrão
            (10 * pesoKg) + (6.25 * alturaCm) - (5 * idade) - 161
        }
    }

    /**
     * Calcula o Gasto Energético Total (GET)
     */
    fun calculateTDEE(bmr: Double, activityLevelDescription: String): Double {
        val activityLevel = ActivityLevel.fromDescription(activityLevelDescription)
        return if (activityLevel != null) {
            bmr * activityLevel.factor
        } else {
            bmr * 1.2 // Retorna sedentário como padrão se a descrição não for encontrada
        }
    }

    /**
     * Calcula os macronutrientes com base na meta calórica.
     * Retorna um Triple com (Proteína em g, Gordura em g, Carboidratos em g)
     */
    fun calculateMacros(targetCalories: Double, pesoKg: Float): Triple<Int, Int, Int> {
        // 1. Calcular Proteína (2.0g por kg de peso)
        val proteinGrams = (pesoKg * 2.0).toInt()
        val proteinCalories = proteinGrams * 4

        // 2. Calcular Gordura (25% do total de calorias)
        val fatCalories = targetCalories * 0.25
        val fatGrams = (fatCalories / 9).toInt()

        // 3. Calcular Carboidratos (o que sobrar)
        val carbCalories = targetCalories - proteinCalories - fatCalories
        val carbGrams = (carbCalories / 4).toInt()

        return Triple(proteinGrams, fatGrams, carbGrams)
    }
}