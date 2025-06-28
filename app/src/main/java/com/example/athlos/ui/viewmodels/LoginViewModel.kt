package com.example.athlos.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    // **MUDANÇA AQUI:** Agora usamos MutableStateFlow e o expomos como StateFlow
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(newEmail: String) {
        // **MUDANÇA AQUI:** Atualizamos o valor do MutableStateFlow
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun updatePassword(newPassword: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun loginUser() {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, loginSuccess = false)
        viewModelScope.launch {
            try {
                authRepository.loginUser(_uiState.value.email, _uiState.value.password) // Acesso direto ao value
                // **MUDANÇA AQUI:**
                _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                Log.d("LoginViewModel", "Login bem-sucedido!")
            } catch (e: Exception) {
                // **MUDANÇA AQUI:**
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (e) {
                        is FirebaseAuthInvalidUserException -> "Usuário não encontrado. Verifique seu email."
                        is FirebaseAuthInvalidCredentialsException -> "Senha incorreta. Tente novamente."
                        else -> "Falha no login: ${e.message}"
                    }
                )
                Log.e("LoginViewModel", "Falha no login: ${e.message}", e)
            }
        }
    }
}