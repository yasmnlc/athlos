package com.example.athlos.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.model.User
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
    val diasTreino: Int = 0
)

class HomeViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    fun refreshUserData() {
        loadUserData()
    }

    private fun loadUserData() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                try {
                    val userData = authRepository.getUserData(user.uid)
                    _uiState.value = _uiState.value.copy(
                        currentUserData = userData,
                        loading = false,
                        aguaAtual = userData?.aguaAtual ?: 0,
                        aguaMeta = userData?.aguaMeta ?: 2000,
                        diasTreino = userData?.diasSemana?.toIntOrNull() ?: 0
                    )
                    Log.d("HomeViewModel", "Dados do usuário carregados: ${userData?.nome}")
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