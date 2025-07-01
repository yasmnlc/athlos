package com.example.athlos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.athlos.api.RetrofitInstance
import com.example.athlos.ui.models.FoodItem
import com.example.athlos.ui.models.toFoodItem
import android.util.Log

class FoodSearchViewModel : ViewModel() {

    var foodList by mutableStateOf<List<FoodItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val appId = "65c865e9"
    private val appKey = "6da5e182dc198e57d36e58df9e04fa68"

    fun searchFood(query: String) {
        if (query.isBlank()) {
            errorMessage = "Digite um alimento válido"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = RetrofitInstance.api.searchFood(
                    appId = appId,
                    appKey = appKey,
                    ingredient = query
                )
                foodList = response.hints.map { it.food.toFoodItem() }

                if (foodList.isEmpty()) {
                    errorMessage = "Nenhum alimento encontrado."
                }
            } catch (e: Exception) {
                Log.e("FoodSearchViewModel", "Erro ao buscar alimento", e)
                errorMessage = "Erro: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearResults() {
        foodList = emptyList()
        errorMessage = null
    }
}