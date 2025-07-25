package com.example.athlos.viewmodels

import android.util.Log
import android.util.Patterns
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
import java.util.Calendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.athlos.utils.await
import com.google.firebase.auth.GoogleAuthProvider

data class RegisterUiState(
    val nome: String = "",
    val dataNascimentoText: String = "",
    val idade: String = "",
    val sexo: String = "",
    val peso: String = "",
    val altura: String = "",
    val email: String = "",
    val senha: String = "",
    val praticaExercicios: Boolean = false,
    val diasSemana: String = "",
    val goal: String = "",
    val sexoExpanded: Boolean = false,
    val erroMensagem: String? = null,
    val carregando: Boolean = false,
    val registroSucesso: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                val userData = authRepository.getUserData(currentUser.uid)
                if (userData != null) {
                    _uiState.value = _uiState.value.copy(
                        nome = userData.nome,
                        email = userData.email
                    )
                }
            }
        }
    }

    fun updateNome(newNome: String) { _uiState.value = _uiState.value.copy(nome = newNome) }
    fun updateDataNascimento(newDateText: String) {
        val formatted = formatarData(newDateText)
        val calculatedAge = if (formatted.length == 10) calcularIdade(formatted) else ""
        _uiState.value = _uiState.value.copy(dataNascimentoText = formatted, idade = calculatedAge)
    }
    fun updateSexo(newSexo: String) { _uiState.value = _uiState.value.copy(sexo = newSexo, sexoExpanded = false) }
    fun toggleSexoDropdown() { _uiState.value = _uiState.value.copy(sexoExpanded = !_uiState.value.sexoExpanded) }
    fun dismissSexoDropdown() { _uiState.value = _uiState.value.copy(sexoExpanded = false) }
    fun updatePeso(newPeso: String) { _uiState.value = _uiState.value.copy(peso = newPeso) }
    fun updateAltura(newAltura: String) { _uiState.value = _uiState.value.copy(altura = newAltura) }
    fun updateEmail(newEmail: String) { _uiState.value = _uiState.value.copy(email = newEmail) }
    fun updateSenha(newSenha: String) { _uiState.value = _uiState.value.copy(senha = newSenha) }
    fun updatePraticaExercicios(newValue: Boolean) { _uiState.value = _uiState.value.copy(praticaExercicios = newValue) }
    fun updateDiasSemana(newDias: String) { _uiState.value = _uiState.value.copy(diasSemana = newDias) }
    fun updateGoal(newGoal: String) { _uiState.value = _uiState.value.copy(goal = newGoal) }

    val dobDay: Int?
        get() = _uiState.value.dataNascimentoText.substringBefore("/", "").toIntOrNull()
    val dobMonth: Int?
        get() = _uiState.value.dataNascimentoText.substringAfter("/").substringBefore("/", "").toIntOrNull()
    val dobYear: Int?
        get() = _uiState.value.dataNascimentoText.substringAfterLast("/").toIntOrNull()

    fun isNameValid(): Boolean = _uiState.value.nome.isNotBlank() && _uiState.value.nome.length >= 3
    fun isDobValid(): Boolean {
        val partes = _uiState.value.dataNascimentoText.split("/")
        if (partes.size != 3) return false
        val day = partes[0].toIntOrNull()
        val month = partes[1].toIntOrNull()
        val year = partes[2].toIntOrNull()
        return day != null && month != null && year != null &&
                year < Calendar.getInstance().get(Calendar.YEAR) &&
                day in 1..31 && month in 1..12
    }
    fun isGenderValid(): Boolean = _uiState.value.sexo.isNotBlank()
    fun isPesoValid(): Boolean = _uiState.value.peso.toFloatOrNull() != null && _uiState.value.peso.toFloat() > 0f
    fun isAlturaValid(): Boolean = _uiState.value.altura.toFloatOrNull() != null && _uiState.value.altura.toFloat() > 0f
    fun isEmailValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()
    fun isPasswordValid(): Boolean = _uiState.value.senha.length >= 6
    fun isExerciseValid(): Boolean = !_uiState.value.praticaExercicios || _uiState.value.diasSemana.isNotBlank()
    fun isGoalValid(): Boolean = _uiState.value.goal.isNotBlank()

    fun registrarUsuarioEPerfil() {
        _uiState.value = _uiState.value.copy(carregando = true, erroMensagem = null, registroSucesso = false)

        viewModelScope.launch {
            try {
                val userMap = mutableMapOf<String, Any>(
                    "nome" to _uiState.value.nome,
                    "dataNascimento" to _uiState.value.dataNascimentoText,
                    "idade" to _uiState.value.idade,
                    "sexo" to _uiState.value.sexo,
                    "peso" to _uiState.value.peso,
                    "altura" to _uiState.value.altura,
                    "email" to _uiState.value.email,
                    "praticaExercicios" to _uiState.value.praticaExercicios,
                    "diasSemana" to _uiState.value.diasSemana,
                    "goal" to _uiState.value.goal,
                    "meta" to "",
                    "aguaAtual" to 0,
                    "aguaMeta" to 2000,
                    "lastResetDate" to LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    "profileImageUrl" to "",
                    "favoriteWorkouts" to emptyList<String>()
                )

                if (!_uiState.value.praticaExercicios) {
                    userMap["diasSemana"] = ""
                }

                authRepository.registerUser(_uiState.value.email, _uiState.value.senha, userMap)

                _uiState.value = _uiState.value.copy(carregando = false, registroSucesso = true)
                Log.d("RegisterViewModel", "Usuário registrado e dados salvos com sucesso no Firestore!")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    carregando = false,
                    erroMensagem = e.localizedMessage ?: "Erro desconhecido ao registrar."
                )
                Log.e("RegisterViewModel", "Erro durante o registro ou ao salvar perfil: ${e.message}", e)
            }
        }
    }

    private fun calcularIdade(data: String): String {
        return try {
            val partes = data.split("/")
            val dia = partes[0].toInt()
            val mes = partes[1].toInt()
            val ano = partes[2].toInt()

            val dataNascimento = Calendar.getInstance().apply { set(ano, mes - 1, dia) }
            val dataAtual = Calendar.getInstance()

            var idadeCalculada = dataAtual.get(Calendar.YEAR) - dataNascimento.get(Calendar.YEAR)
            if (dataAtual.get(Calendar.DAY_OF_YEAR) < dataNascimento.get(Calendar.DAY_OF_YEAR)) {
                idadeCalculada--
            }
            "$idadeCalculada anos"
        } catch (e: Exception) {
            Log.e("RegisterViewModel", "Erro ao calcular idade: ${e.message}")
            ""
        }
    }

    private fun formatarData(input: String): String {
        val numbers = input.filter { it.isDigit() }.take(8)
        return buildString {
            for (i in numbers.indices) {
                append(numbers[i])
                if ((i == 1 || i == 3) && i != numbers.lastIndex) append('/')
            }
        }
    }

    fun salvarDadosAdicionaisDoPerfil() {
        _uiState.value = _uiState.value.copy(carregando = true, erroMensagem = null)
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    val additionalData = mapOf(
                        "dataNascimento" to _uiState.value.dataNascimentoText,
                        "idade" to _uiState.value.idade,
                        "sexo" to _uiState.value.sexo,
                        "peso" to _uiState.value.peso,
                        "altura" to _uiState.value.altura,
                        "praticaExercicios" to _uiState.value.praticaExercicios,
                        "diasSemana" to _uiState.value.diasSemana,
                        "goal" to _uiState.value.goal
                    )
                    // Usamos merge = true para não sobrescrever os dados básicos já salvos
                    authRepository.updateUserData(currentUser.uid, additionalData, merge = true)

                    _uiState.value = _uiState.value.copy(carregando = false, registroSucesso = true)
                    Log.d("RegisterViewModel", "Dados adicionais do perfil salvos com sucesso.")

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erroMensagem = "Falha ao salvar dados: ${e.message}"
                    )
                    Log.e("RegisterViewModel", "Erro ao salvar dados adicionais: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(carregando = false, erroMensagem = "Nenhum usuário logado.")
            }
        }
    }

    fun isCurrentUserGoogleUser(): Boolean {
        val currentUser = authRepository.currentUser
        return currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
    }
}