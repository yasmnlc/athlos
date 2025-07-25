package com.example.athlos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.local.AppDatabase
import com.example.athlos.data.repository.DiaryRepository
import com.example.athlos.ui.models.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val foodEntries: List<FoodItem> = emptyList(),
    val isLoading: Boolean = true
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val diaryRepository: DiaryRepository
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        // Obtenha as instâncias necessárias
        val foodDao = AppDatabase.getDatabase(application).foodEntryDao()
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        // Passe todas as dependências para o repositório
        diaryRepository = DiaryRepository(
            foodEntryDao = foodDao,
            firestore = firestore,
            auth = auth
        )

        // O resto da lógica continua a mesma
        viewModelScope.launch {
            diaryRepository.getEntriesForDate(LocalDate.now())
                .collect { entries ->
                    _uiState.update {
                        it.copy(foodEntries = entries, isLoading = false)
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