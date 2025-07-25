package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.model.User
import com.example.athlos.ui.models.CustomWorkout // Garante que o tipo correto está sendo usado
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.athlos.utils.NutritionalCalculator

data class HomeUiState(
    val currentUserData: User? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val aguaAtual: Int = 0,
    val aguaMeta: Int = 2000,
    val diasTreino: Int = 0,
    val savedWorkouts: List<CustomWorkout> = emptyList(), // O tipo está correto aqui
    val tdee: Double = 0.0,
    val caloriesForLoss: Double = 0.0,
    val caloriesForGain: Double = 0.0,
    val macrosForLoss: MacroTargets = MacroTargets(),
    val macrosForGain: MacroTargets = MacroTargets()
)

data class MacroTargets(
    val proteinGrams: Int = 0,
    val fatGrams: Int = 0,
    val carbGrams: Int = 0
)

class HomeViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserDataAndSavedWorkouts()
    }

    fun refreshUserData() {
        loadUserDataAndSavedWorkouts()
    }

    private fun loadUserDataAndSavedWorkouts() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                try {
                    val userData = authRepository.getUserData(user.uid)
                    // CORREÇÃO: Carrega diretamente os treinos salvos do tipo CustomWorkout
                    val savedWorkouts = authRepository.getCustomWorkouts()

                    if (userData != null) {
                        // ... (lógica de cálculo nutricional - sem alterações)
                        val peso = userData.peso.toFloatOrNull()
                        val altura = userData.altura.toFloatOrNull()
                        val idade = userData.idade.replace(" anos", "").toIntOrNull()
                        val sexo = userData.sexo
                        val nivelAtividade = userData.diasSemana
                        var tdee = 0.0
                        var caloriesForLoss = 0.0
                        var caloriesForGain = 0.0
                        var macrosForLoss = MacroTargets()
                        var macrosForGain = MacroTargets()
                        if (peso != null && altura != null && idade != null && sexo.isNotBlank() && !nivelAtividade.isNullOrBlank()) {
                            val bmr = NutritionalCalculator.calculateBMR(peso, altura, idade, sexo)
                            tdee = NutritionalCalculator.calculateTDEE(bmr, nivelAtividade)
                            caloriesForLoss = tdee * 0.85
                            caloriesForGain = tdee * 1.15
                            val (lossProtein, lossFat, lossCarb) = NutritionalCalculator.calculateMacros(caloriesForLoss, peso)
                            macrosForLoss = MacroTargets(lossProtein, lossFat, lossCarb)
                            val (gainProtein, gainFat, gainCarb) = NutritionalCalculator.calculateMacros(caloriesForGain, peso)
                            macrosForGain = MacroTargets(gainProtein, gainFat, gainCarb)
                        }

                        // CORREÇÃO: A lógica antiga que criava uma lista de `Workout` foi removida.
                        // Agora passamos a lista correta `savedWorkouts` para o estado.
                        _uiState.value = _uiState.value.copy(
                            currentUserData = userData,
                            loading = false,
                            aguaAtual = userData.aguaAtual ?: 0,
                            aguaMeta = userData.aguaMeta ?: 2000,
                            diasTreino = userData.diasSemana?.toIntOrNull() ?: 0,
                            savedWorkouts = savedWorkouts,
                            tdee = tdee,
                            caloriesForLoss = caloriesForLoss,
                            caloriesForGain = caloriesForGain,
                            macrosForLoss = macrosForLoss,
                            macrosForGain = macrosForGain
                        )
                        Log.d("HomeViewModel", "Dados do usuário e ${savedWorkouts.size} treinos salvos carregados.")
                    } else {
                        _uiState.value = _uiState.value.copy(loading = false, errorMessage = "Não foi possível carregar os dados do usuário.")
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao carregar dados: ${e.message}",
                        loading = false
                    )
                    Log.e("HomeViewModel", "Erro ao carregar dados: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Nenhum usuário logado.", loading = false)
            }
        }
    }
}