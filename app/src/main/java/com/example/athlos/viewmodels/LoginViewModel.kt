package com.example.athlos.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.athlos.utils.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val isAuthenticated: Boolean = false,
    val successMessage: String? = null,
    val googleNavDestination: GoogleLoginNavigation = GoogleLoginNavigation.NONE
)

enum class GoogleLoginNavigation {
    NONE,
    TO_MAIN,
    TO_REGISTRATION
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository = FirebaseAuthRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("532262747843-b2o04u39q7dogkdha0tcsbl7dn4jg3n5.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(getApplication(), gso)
    }

    fun updateEmail(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun updatePassword(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun loginUser() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, loginSuccess = false)
        viewModelScope.launch {
            try {
                authRepository.loginUser(_uiState.value.email, _uiState.value.password)
                _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                Log.d("LoginViewModel", "Login bem-sucedido!")
            } catch (e: Exception) {
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

    fun getGoogleSignInIntent() = googleSignInClient.signInIntent

    fun handleGoogleSignInResult(account: GoogleSignInAccount?, exception: Exception?) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, googleNavDestination = GoogleLoginNavigation.NONE)
        viewModelScope.launch {
            if (account != null && account.idToken != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                try {
                    // Autentica com o Firebase
                    val firebaseUser = authRepository.signInWithCredential(credential)
                    if (firebaseUser != null) {
                        // VERIFICAÇÃO NO FIRESTORE
                        val userData = authRepository.getUserData(firebaseUser.uid)

                        if (userData != null && !userData.peso.isNullOrBlank()) {
                            // Usuário já existe e tem dados, vá para a tela principal
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthenticated = true, // Mantemos para compatibilidade se precisar
                                googleNavDestination = GoogleLoginNavigation.TO_MAIN
                            )
                            Log.d("LoginViewModel", "Usuário do Google já cadastrado. Navegando para main.")
                        } else {
                            // Primeiro login com Google ou dados incompletos.
                            // Salva os dados básicos (nome, email) e navega para o fluxo de cadastro.
                            val basicUserData = mapOf(
                                "nome" to (account.displayName ?: ""),
                                "email" to (account.email ?: ""),
                                "profileImageUrl" to (account.photoUrl?.toString() ?: "")
                            )
                            authRepository.updateUserData(firebaseUser.uid, basicUserData, merge = true)

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthenticated = true, // Mantemos para compatibilidade
                                googleNavDestination = GoogleLoginNavigation.TO_REGISTRATION
                            )
                            Log.d("LoginViewModel", "Primeiro login com Google. Navegando para o fluxo de cadastro.")
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha na autenticação Firebase com Google: Usuário nulo.")
                        Log.e("LoginViewModel", "Falha na autenticação Firebase com Google: Usuário nulo.")
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Falha na autenticação Firebase com Google: ${e.message}"
                    )
                    Log.e("LoginViewModel", "Erro ao autenticar Firebase com Google: ${e.message}", e)
                }
            } else if (exception != null) {
                val errorMessage = (exception as? ApiException)?.statusCode?.let {
                    "Erro no Google Sign-In: Código $it"
                } ?: "Erro desconhecido no Google Sign-In: ${exception.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
                Log.e("LoginViewModel", errorMessage, exception)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login com Google cancelado ou falhou."
                )
                Log.d("LoginViewModel", "Login com Google cancelado ou falhou sem exceção.")
            }
        }
    }

    fun signOutGoogle() {
        googleSignInClient.revokeAccess().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginViewModel", "Google Sign-In sessão limpa.")
            } else {
                Log.e("LoginViewModel", "Falha ao limpar sessão Google Sign-In: ${task.exception?.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun onNavigationComplete() {
        _uiState.value = _uiState.value.copy(googleNavDestination = GoogleLoginNavigation.NONE)
    }
}