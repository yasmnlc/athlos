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

data class HomeUiState(
    val currentUserData: User? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val aguaAtual: Int = 0,
    val aguaMeta: Int = 2000,
    val diasTreino: Int = 0,
    val favoriteWorkouts: List<Workout> = emptyList()
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
                    val loadedAguaAtual = userData?.aguaAtual ?: 0
                    val loadedAguaMeta = userData?.aguaMeta ?: 2000
                    val diasTreino = userData?.diasSemana?.toIntOrNull() ?: 0

                    val favoriteWorkoutIds = userData?.favoriteWorkouts ?: emptyList()

                    val allMockWorkouts = listOf(
                        Workout("treino_peito", "Treino de Peito", "Desenvolvimento, Supino, Crucifixo", R.drawable.chest, false),
                        Workout("treino_costas", "Treino de Costas", "Puxada, Remada, Levantamento Terra", R.drawable.back, false),
                        Workout("treino_quadriceps", "Treino de Quadríceps", "Agachamento, Leg Press, Extensora", R.drawable.quads, false),
                        Workout("treino_ombros", "Treino de Ombros", "Elevação lateral, desenvolvimento", R.drawable.shoulder, false),
                        Workout("treino_biceps", "Treino de Bíceps", "Rosca Direta, Rosca Alternada, Rosca Concentrada", R.drawable.biceps, false),
                        Workout("treino_triceps", "Treino de Tríceps", "Extensão, Tríceps Testa, Mergulho", R.drawable.triceps, false),
                        Workout("treino_abdomen", "Treino de Abdômen", "Abdominal, Prancha, Elevação de Pernas", R.drawable.abs, false),
                        Workout("treino_gluteos", "Treino de Glúteos", "Agachamento, Glúteo Máquina, Elevação Pélvica", R.drawable.glutes, false),
                        Workout("treino_dorsal", "Treino de Dorsal", "Puxada, Remada, Pullover", R.drawable.dorsal, false),
                        Workout("treino_posterior", "Treino de Posterior", "Stiff, Flexora, Bom Dia", R.drawable.hamstrings, false),
                        Workout("treino_obliquos", "Treino de Oblíquos", "Rotação de Tronco, Flexão Lateral", R.drawable.obliquo, false),
                        Workout("treino_trapezio", "Treino de Trapézio", "Remada Alta, Encolhimento", R.drawable.trapezius, false),
                        Workout("treino_panturrilha", "Treino de Panturrilha", "Elevação em Pé, Elevação Sentado", R.drawable.calfs, false),
                        Workout("treino_antebraco", "Treino de Antebraço", "Rosca Punho, Flexão Inversa", R.drawable.forearm, false)
                    )

                    val favoritedWorkouts = allMockWorkouts.filter { workout ->
                        favoriteWorkoutIds.contains(workout.id)
                    }.map { workout ->
                        workout.copy(isFavorite = true)
                    }

                    _uiState.value = _uiState.value.copy(
                        currentUserData = userData,
                        loading = false,
                        aguaAtual = loadedAguaAtual,
                        aguaMeta = loadedAguaMeta,
                        diasTreino = diasTreino,
                        favoriteWorkouts = favoritedWorkouts
                    )
                    Log.d("HomeViewModel", "Dados do usuário e treinos favoritos carregados: ${favoritedWorkouts.map { it.id }}")
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