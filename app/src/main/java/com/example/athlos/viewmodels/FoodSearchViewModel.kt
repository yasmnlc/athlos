package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.api.OpenFoodFactsService
import com.example.athlos.ui.models.FoodItem
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.tasks.await

class FoodSearchViewModel : ViewModel() {

    var foodList by mutableStateOf<List<FoodItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val translator: Translator

    init {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.PORTUGUESE)
            .build()
        translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { Log.d("Translator", "Modelo de PT baixado com sucesso.") }
            .addOnFailureListener { e -> Log.e("Translator", "Erro ao baixar modelo de PT.", e) }
    }

    private suspend fun translateText(text: String): String {
        val isLikelyEnglish = text.contains(Regex("[kKyYwW]")) || !text.contains(Regex("[çãõéáíóú]"))
        if (!isLikelyEnglish && text.split(" ").size < 3) {
            return text.replaceFirstChar { it.uppercaseChar() }
        }

        return try {
            translator.translate(text).await().replaceFirstChar { it.uppercaseChar() }
        } catch (e: Exception) {
            Log.e("Translator", "Erro ao traduzir '$text'", e)
            text.replaceFirstChar { it.uppercaseChar() }
        }
    }

    fun searchFood(query: String) {
        if (query.isBlank()) {
            errorMessage = "Digite um alimento válido"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = OpenFoodFactsService.api.searchFoods(query)
                Log.d("API", "Resposta recebida com ${response.products.size} produtos.")

                val results = response.products
                    .filter { it.product_name != null && it.nutriments != null }
                    .take(10)
                    .mapNotNull { product ->
                        try {
                            val translatedName = product.product_name?.let { translateText(it) } ?: "Desconhecido"

                            // ALTERAÇÃO AQUI: Mapeando para a nova estrutura do FoodItem
                            val item = FoodItem(
                                name = translatedName,
                                baseCalories = product.nutriments?.energyKcal100g?.toInt() ?: 0,
                                baseProtein = product.nutriments?.proteins_100g?.toDouble() ?: 0.0,
                                baseCarbohydrate = product.nutriments?.carbohydrates_100g?.toDouble() ?: 0.0,
                                baseFat = product.nutriments?.fat_100g?.toDouble() ?: 0.0,
                                baseFiber = product.nutriments?.fiber_100g?.toDouble() ?: 0.0,
                                grams = 100
                            )
                            Log.d("API", "Item mapeado e traduzido: $item")
                            item
                        } catch (e: Exception) {
                            Log.e("MapError", "Erro ao mapear produto: ${product.product_name}", e)
                            null
                        }
                    }

                foodList = results

                if (results.isEmpty()) {
                    errorMessage = "Nenhum alimento encontrado."
                    Log.w("API", "Lista final de alimentos está vazia.")
                }

            } catch (e: Exception) {
                Log.e("FoodSearchViewModel", "Erro ao buscar alimento", e)
                errorMessage = "Erro de rede ou conversão: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearResults() {
        foodList = emptyList()
        errorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        translator.close()
    }
}