package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.api.ExerciseDbService
import com.example.athlos.api.YouTubeService
import com.example.athlos.ui.models.Exercise
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

class ExerciseViewModel : ViewModel() {

    private val exerciseDbApiKey = "69a1f86bdfmsh0a6e1a654dae000p197607jsn6acb47efc61e"
    private val youTubeApiKey = "AIzaSyDF71ScCOEuw-sm9qAlAIJ7vF-X79mt978"

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    fun getExercises(bodyPart: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Passo 1: Busca os exercícios da ExerciseDB
                val originalExercises = ExerciseDbService.api.getExercisesByBodyPart(
                    bodyPart = bodyPart.lowercase(),
                    apiKey = exerciseDbApiKey
                )

                // Passo 2: Para cada exercício, busca o vídeo no YouTube usando o nome em inglês
                val exercisesWithVideos = coroutineScope {
                    originalExercises.map { exercise ->
                        async {
                            val videoId = findYouTubeVideoId(exercise.name)
                            exercise.copy(videoId = videoId)
                        }
                    }.awaitAll()
                }

                _uiState.update { it.copy(isLoading = false, exerciseList = exercisesWithVideos) }
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Erro CRÍTICO no processo de busca", e)
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