package com.example.athlos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.local.AppDatabase
import com.example.athlos.data.repository.DiaryRepository
import com.example.athlos.ui.models.FoodItem
import com.example.athlos.utils.NutritionalCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val foodEntries: List<FoodItem> = emptyList(),
    val isLoading: Boolean = true,
    val calorieGoal: Int = 2200, // Valores padrão
    val proteinGoal: Double = 120.0,
    val carbGoal: Double = 250.0,
    val fatGoal: Double = 70.0
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val diaryRepository: DiaryRepository
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        val foodDao = AppDatabase.getDatabase(application).foodEntryDao()
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        diaryRepository = DiaryRepository(
            foodEntryDao = foodDao,
            firestore = firestore,
            auth = auth
        )

        loadGoalsAndEntriesForDate(LocalDate.now())
    }
    private fun loadGoalsAndEntriesForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Carregar dados do usuário para calcular metas
            val user = diaryRepository.getUserData()
            if (user != null) {
                val peso = user.peso.toFloatOrNull()
                val altura = user.altura.toFloatOrNull()
                val idade = user.idade.replace(" anos", "").toIntOrNull()
                val sexo = user.sexo
                val nivelAtividade = user.diasSemana
                val goal = user.goal

                if (peso != null && altura != null && idade != null && sexo.isNotBlank() && !nivelAtividade.isNullOrBlank() && !goal.isNullOrBlank()) {
                    val bmr = NutritionalCalculator.calculateBMR(peso, altura, idade, sexo)
                    val tdee = NutritionalCalculator.calculateTDEE(bmr, nivelAtividade)

                    val targetCalories = when (goal) {
                        "Perder Peso" -> tdee * 0.85 // Déficit 15%
                        "Ganhar Peso" -> tdee * 1.15 // Superávit 15%
                        else -> tdee // Manter Peso
                    }

                    val (protein, fat, carb) = NutritionalCalculator.calculateMacros(targetCalories, peso)

                    _uiState.update {
                        it.copy(
                            calorieGoal = targetCalories.toInt(),
                            proteinGoal = protein.toDouble(),
                            fatGoal = fat.toDouble(),
                            carbGoal = carb.toDouble()
                        )
                    }
                }
            }

            // 2. Carregar as entradas de comida do diário
            diaryRepository.getEntriesForDate(date).collect { entries ->
                _uiState.update {
                    it.copy(foodEntries = entries, isLoading = false, selectedDate = date)
                }
            }
        }
    }

    fun addFood(foodItem: FoodItem) = viewModelScope.launch {
        diaryRepository.addFoodEntry(foodItem)
    }

    fun updateFood(foodItem: FoodItem) = viewModelScope.launch {
        diaryRepository.updateFoodEntry(foodItem)
    }

    fun deleteFood(foodItem: FoodItem) = viewModelScope.launch {
        diaryRepository.deleteFoodEntry(foodItem)
    }
}