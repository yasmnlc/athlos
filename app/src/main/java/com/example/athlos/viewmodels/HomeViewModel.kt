package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.R
import com.example.athlos.data.model.User
import com.example.athlos.data.model.Workout
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
    val favoriteWorkouts: List<Workout> = emptyList(),
    val tdee: Double = 0.0, // Gasto Energético Total (Manter peso)
    val caloriesForLoss: Double = 0.0, // Calorias para perder peso
    val caloriesForGain: Double = 0.0, // Calorias para ganhar peso
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
        loadUserDataAndFavoriteWorkouts()
    }

    fun refreshUserData() {
        loadUserDataAndFavoriteWorkouts()
    }

    private fun loadUserDataAndFavoriteWorkouts() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                try {
                    val userData = authRepository.getUserData(user.uid)

                    if (userData != null) {
                        val loadedAguaAtual = userData.aguaAtual ?: 0
                        val loadedAguaMeta = userData.aguaMeta ?: 2000
                        val diasTreino = userData.diasSemana?.toIntOrNull() ?: 0
                        val favoriteWorkoutIds = userData.favoriteWorkouts ?: emptyList()

                        val allMockWorkouts = listOf(
                            Workout(
                                "treino_peito",
                                "Treino de Peito",
                                "Desenvolvimento, Supino, Crucifixo",
                                R.drawable.chest,
                                false
                            ),
                            Workout(
                                "treino_costas",
                                "Treino de Costas",
                                "Puxada, Remada, Levantamento Terra",
                                R.drawable.back,
                                false
                            ),
                            Workout(
                                "treino_quadriceps",
                                "Treino de Quadríceps",
                                "Agachamento, Leg Press, Extensora",
                                R.drawable.quads,
                                false
                            ),
                            Workout(
                                "treino_ombros",
                                "Treino de Ombros",
                                "Elevação lateral, desenvolvimento",
                                R.drawable.shoulder,
                                false
                            ),
                            Workout(
                                "treino_biceps",
                                "Treino de Bíceps",
                                "Rosca Direta, Rosca Alternada, Rosca Concentrada",
                                R.drawable.biceps,
                                false
                            ),
                            Workout(
                                "treino_triceps",
                                "Treino de Tríceps",
                                "Extensão, Tríceps Testa, Mergulho",
                                R.drawable.triceps,
                                false
                            ),
                            Workout(
                                "treino_abdomen",
                                "Treino de Abdômen",
                                "Abdominal, Prancha, Elevação de Pernas",
                                R.drawable.abs,
                                false
                            ),
                            Workout(
                                "treino_gluteos",
                                "Treino de Glúteos",
                                "Agachamento, Glúteo Máquina, Elevação Pélvica",
                                R.drawable.glutes,
                                false
                            ),
                            Workout(
                                "treino_dorsal",
                                "Treino de Dorsal",
                                "Puxada, Remada, Pullover",
                                R.drawable.dorsal,
                                false
                            ),
                            Workout(
                                "treino_posterior",
                                "Treino de Posterior",
                                "Stiff, Flexora, Bom Dia",
                                R.drawable.hamstrings,
                                false
                            ),
                            Workout(
                                "treino_obliquos",
                                "Treino de Oblíquos",
                                "Rotação de Tronco, Flexão Lateral",
                                R.drawable.obliquo,
                                false
                            ),
                            Workout(
                                "treino_trapezio",
                                "Treino de Trapézio",
                                "Remada Alta, Encolhimento",
                                R.drawable.trapezius,
                                false
                            ),
                            Workout(
                                "treino_panturrilha",
                                "Treino de Panturrilha",
                                "Elevação em Pé, Elevação Sentado",
                                R.drawable.calfs,
                                false
                            ),
                            Workout(
                                "treino_antebraco",
                                "Treino de Antebraço",
                                "Rosca Punho, Flexão Inversa",
                                R.drawable.forearm,
                                false
                            )
                        )

                        val favoritedWorkouts = allMockWorkouts.filter { workout ->
                            favoriteWorkoutIds.contains(workout.id)
                        }.map { workout ->
                            workout.copy(isFavorite = true)
                        }

                        // --- INÍCIO DA LÓGICA DE CÁLCULO ADICIONADA ---
                        val peso = userData.peso.toFloatOrNull()
                        val altura = userData.altura.toFloatOrNull()
                        // Remove " anos" da string de idade antes de converter
                        val idade = userData.idade.replace(" anos", "").toIntOrNull()
                        val sexo = userData.sexo
                        val nivelAtividade = userData.diasSemana

                        var tdee = 0.0
                        var caloriesForLoss = 0.0
                        var caloriesForGain = 0.0
                        var macrosForLoss: MacroTargets = MacroTargets()
                        var macrosForGain: MacroTargets = MacroTargets()

                        // Garante que todos os dados necessários para o cálculo existem
                        if (peso != null && altura != null && idade != null && sexo.isNotBlank() && !nivelAtividade.isNullOrBlank()) {
                            val bmr = NutritionalCalculator.calculateBMR(peso, altura, idade, sexo)
                            tdee = NutritionalCalculator.calculateTDEE(bmr, nivelAtividade)

                            caloriesForLoss = tdee * 0.85 // Déficit de 15%
                            caloriesForGain = tdee * 1.15 // Superávit de 15%

                            val (lossProtein, lossFat, lossCarb) = NutritionalCalculator.calculateMacros(
                                caloriesForLoss,
                                peso
                            )
                            macrosForLoss = MacroTargets(lossProtein, lossFat, lossCarb)

                            val (gainProtein, gainFat, gainCarb) = NutritionalCalculator.calculateMacros(
                                caloriesForGain,
                                peso
                            )
                            macrosForGain = MacroTargets(gainProtein, gainFat, gainCarb)
                        }
                        // --- FIM DA LÓGICA DE CÁLCULO ---

                        // ATUALIZA O ESTADO COM TODOS OS DADOS (ANTIGOS E NOVOS)
                        _uiState.value = _uiState.value.copy(
                            currentUserData = userData,
                            loading = false,
                            aguaAtual = loadedAguaAtual,
                            aguaMeta = loadedAguaMeta,
                            diasTreino = diasTreino,
                            favoriteWorkouts = favoritedWorkouts,
                            // Adicionando os novos valores calculados
                            tdee = tdee,
                            caloriesForLoss = caloriesForLoss,
                            caloriesForGain = caloriesForGain,
                            macrosForLoss = macrosForLoss,
                            macrosForGain = macrosForGain
                        )
                        Log.d(
                            "HomeViewModel",
                            "Dados do usuário e cálculos nutricionais carregados."
                        )

                    } else {
                        // Caso userData seja nulo
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            errorMessage = "Não foi possível carregar os dados do usuário."
                        )
                    }

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao carregar dados do usuário: ${e.message}",
                        loading = false
                    )
                    Log.e("HomeViewModel", "Erro ao carregar dados do usuário: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado.",
                    loading = false
                )
                Log.d("HomeViewModel", "Nenhum usuário logado na HomeScreen.")
            }
        }
    }
}