// ProfileViewModel.kt
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

data class ProfileUiState(
    val userData: User? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val meta: String = ""
)

class ProfileViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    val userProfile = authRepository.getUserData(currentUser.uid)
                    _uiState.value = _uiState.value.copy(
                        userData = userProfile,
                        meta = userProfile?.meta ?: "",
                        loading = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Falha ao carregar dados do perfil: ${e.message}",
                        loading = false
                    )
                    Log.e("ProfileViewModel", "Error loading user profile: ${e.message}", e)
                }
            } else {
                // **MUDANÇA AQUI:**
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado.",
                    loading = false
                )
            }
        }
    }

    fun updateMeta(newMeta: String) {
        _uiState.value = _uiState.value.copy(meta = newMeta)
    }

    fun saveMeta() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    authRepository.updateUserData(currentUser.uid, mapOf("meta" to _uiState.value.meta))
                    Log.d("ProfileViewModel", "Meta saved successfully!")
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error saving meta: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(errorMessage = "Falha ao salvar meta: ${e.message}")
                }
            }
        }
    }

    fun logoutUser() {
        authRepository.logoutUser()
        _uiState.value = _uiState.value.copy(userData = null, meta = "", loading = false)
    }
}