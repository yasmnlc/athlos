package com.example.athlos.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.model.User
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.athlos.utils.await

data class ProfileUiState(
    val userData: User? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val profileImageUrl: String? = null,
    val isUploadingPhoto: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
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
                    val imageUrl = userProfile?.profileImageUrl

                    _uiState.value = _uiState.value.copy(
                        userData = userProfile,
                        profileImageUrl = imageUrl,
                        loading = false
                    )
                    Log.d("ProfileViewModel", "Perfil do usuário carregado: ${userProfile?.nome}, Foto: $imageUrl")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Falha ao carregar dados do perfil: ${e.message}",
                        loading = false
                    )
                    Log.e("ProfileViewModel", "Erro ao carregar perfil do usuário: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado.",
                    loading = false
                )
                Log.d("ProfileViewModel", "Nenhum usuário logado no ProfileScreen.")
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        _uiState.value = _uiState.value.copy(isUploadingPhoto = true, errorMessage = null)
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    val storageRef = storage.reference.child("profile_images/${currentUser.uid}.jpg")
                    val uploadTask = storageRef.putFile(imageUri).await()
                    val downloadUrl = storageRef.downloadUrl.await().toString()

                    // Salva a URL da imagem no Firestore do usuário
                    authRepository.updateUserData(currentUser.uid, mapOf("profileImageUrl" to downloadUrl))

                    _uiState.value = _uiState.value.copy(
                        profileImageUrl = downloadUrl,
                        isUploadingPhoto = false
                    )
                    Log.d("ProfileViewModel", "Foto de perfil enviada e URL salva: $downloadUrl")

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Falha ao enviar foto: ${e.message}",
                        isUploadingPhoto = false
                    )
                    Log.e("ProfileViewModel", "Erro ao enviar foto: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado para enviar a foto.",
                    isUploadingPhoto = false
                )
            }
        }
    }

    fun logoutUser() {
        authRepository.logoutUser()
        _uiState.value = ProfileUiState()
        Log.d("ProfileViewModel", "Usuário deslogado. Estado do perfil resetado.")
    }
}