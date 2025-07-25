package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.api.ExerciseDbService
import com.example.athlos.api.YouTubeService
import com.example.athlos.data.repository.ExerciseRepository
import com.example.athlos.data.repository.FirestoreExerciseRepository
import com.example.athlos.ui.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseUiState(
    val exerciseList: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExerciseViewModel(
    // Injetando o repositório para interagir com o Firestore
    private val exerciseRepository: ExerciseRepository = FirestoreExerciseRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val exerciseDbApiKey = "69a1f86bdfmsh0a6e1a654dae000p197607jsn6acb47efc61e"
    private val youTubeApiKey = "AIzaSyDF71ScCOEuw-sm9qAlAIJ7vF-X79mt978"

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    fun getExercises(bodyPart: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Tenta buscar a lista de exercícios do cache (Firestore) primeiro
                var exercisesFromCache = exerciseRepository.getExercisesByBodyPart(bodyPart.lowercase())

                // 2. CACHE MISS: Se a lista do cache estiver vazia, busca na API
                if (exercisesFromCache.isEmpty()) {
                    Log.d("ExerciseVM", "Cache MISS para bodyPart '$bodyPart'. Buscando na API...")
                    val exercisesFromApi = ExerciseDbService.api.getExercisesByBodyPart(
                        bodyPart = bodyPart.lowercase(),
                        apiKey = exerciseDbApiKey
                    )

                    // 3. Salva cada exercício da API no cache do Firestore
                    exercisesFromApi.forEach { exercise ->
                        exerciseRepository.saveExercise(exercise)
                    }
                    Log.d("ExerciseVM", "${exercisesFromApi.size} exercícios para '$bodyPart' salvos no cache.")
                    exercisesFromCache = exercisesFromApi
                } else {
                    Log.d("ExerciseVM", "Cache HIT para bodyPart '$bodyPart'. ${exercisesFromCache.size} exercícios carregados do Firestore.")
                }

                // 4. Lógica de Cache para Vídeos do YouTube
                val exercisesWithVideos = coroutineScope {
                    exercisesFromCache.map { exercise ->
                        async {
                            // 5. Se o vídeo não estiver no cache (videoId é nulo), busca na API do YouTube
                            if (exercise.videoId == null) {
                                Log.d("ExerciseVM", "YouTube Cache MISS para '${exercise.name}'. Buscando vídeo...")
                                val videoId = findYouTubeVideoId(exercise.name)
                                if (videoId != null) {
                                    // 6. Atualiza o exercício com o ID do vídeo e salva de volta no cache
                                    val updatedExercise = exercise.copy(videoId = videoId)
                                    exerciseRepository.saveExercise(updatedExercise)
                                    updatedExercise // Retorna o exercício atualizado
                                } else {
                                    exercise // Retorna o original se não encontrar vídeo
                                }
                            } else {
                                Log.d("ExerciseVM", "YouTube Cache HIT para '${exercise.name}'.")
                                exercise // Retorna o exercício do cache que já tem o vídeo
                            }
                        }
                    }.awaitAll()
                }

                _uiState.update { it.copy(isLoading = false, exerciseList = exercisesWithVideos) }
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Erro CRÍTICO no processo de busca para '$bodyPart'", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Falha ao carregar exercícios: ${e.message}")
                }
            }
        }
    }

    private suspend fun findYouTubeVideoId(exerciseName: String?): String? {
        if (exerciseName == null) return null
        return try {
            val searchQuery = "$exerciseName exercise tutorial"
            val response = YouTubeService.api.searchVideos(query = searchQuery, apiKey = youTubeApiKey)
            val videoId = response.items.firstOrNull()?.id?.videoId
            Log.d("YouTubeSearch", "Vídeo encontrado para '$searchQuery': $videoId")
            videoId
        } catch (e: Exception) {
            Log.e("YouTubeSearch", "Erro ao buscar vídeo para '$exerciseName'", e)
            null
        }
    }
}