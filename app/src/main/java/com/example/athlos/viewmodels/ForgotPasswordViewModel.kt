package com.example.athlos.viewmodels

import android.util.Log
import android.util.Patterns // Para validação de e-mail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val emailSent: Boolean = false // Indica se o e-mail foi enviado com sucesso
)

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun isEmailValid(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()
    }

    fun sendPasswordResetEmail() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null, emailSent = false)
        viewModelScope.launch {
            if (!isEmailValid()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Por favor, insira um e-mail válido."
                )
                return@launch
            }

            try {
                authRepository.sendPasswordResetEmail(_uiState.value.email) // Chama o método do repositório
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Um e-mail de redefinição de senha foi enviado para ${_uiState.value.email}.",
                    emailSent = true
                )
                Log.d("ForgotPasswordVM", "E-mail de redefinição enviado para: ${_uiState.value.email}")
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Este e-mail não está registrado. Por favor, verifique."
                    else -> "Erro ao enviar e-mail: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
                Log.e("ForgotPasswordVM", "Erro ao enviar e-mail de redefinição: ${e.message}", e)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}