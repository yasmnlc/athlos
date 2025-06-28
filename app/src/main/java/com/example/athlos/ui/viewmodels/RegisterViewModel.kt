package com.example.athlos.ui.viewmodels

import android.util.Log
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

    fun updateNome(newNome: String) {
        _uiState.value = _uiState.value.copy(nome = newNome)
    }

    fun updateDataNascimento(newDateText: String) {
        val formatted = formatarData(newDateText)
        val calculatedAge = if (formatted.length == 10) calcularIdade(formatted) else ""
        _uiState.value = _uiState.value.copy(dataNascimentoText = formatted, idade = calculatedAge)
    }

    fun updateSexo(newSexo: String) {
        _uiState.value = _uiState.value.copy(sexo = newSexo, sexoExpanded = false)
    }

    fun toggleSexoDropdown() {
        _uiState.value = _uiState.value.copy(sexoExpanded = !_uiState.value.sexoExpanded)
    }

    fun dismissSexoDropdown() {
        _uiState.value = _uiState.value.copy(sexoExpanded = false)
    }

    fun updatePeso(newPeso: String) {
        _uiState.value = _uiState.value.copy(peso = newPeso)
    }

    fun updateAltura(newAltura: String) {
        _uiState.value = _uiState.value.copy(altura = newAltura)
    }

    fun updateEmail(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun updateSenha(newSenha: String) {
        _uiState.value = _uiState.value.copy(senha = newSenha)
    }

    fun updatePraticaExercicios(newValue: Boolean) {
        _uiState.value = _uiState.value.copy(praticaExercicios = newValue)
    }

    fun updateDiasSemana(newDias: String) {
        _uiState.value = _uiState.value.copy(diasSemana = newDias)
    }

    fun registrarUsuario() {
        _uiState.value = _uiState.value.copy(carregando = true, erroMensagem = null, registroSucesso = false)

        viewModelScope.launch {
            try {
                val userDataMap = mapOf(
                    "nome" to _uiState.value.nome, // Acesso direto ao value
                    "dataNascimento" to _uiState.value.dataNascimentoText,
                    "idade" to _uiState.value.idade,
                    "sexo" to _uiState.value.sexo,
                    "peso" to _uiState.value.peso,
                    "altura" to _uiState.value.altura,
                    "email" to _uiState.value.email,
                    "praticaExercicios" to _uiState.value.praticaExercicios,
                    "diasSemana" to _uiState.value.diasSemana
                )

                authRepository.registerUser(_uiState.value.email, _uiState.value.senha, userDataMap)

                _uiState.value = _uiState.value.copy(carregando = false, registroSucesso = true)
                Log.d("RegisterViewModel", "Usuário registrado e dados salvos com sucesso!")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(carregando = false, erroMensagem = "Erro no cadastro: ${e.message}")
                Log.e("RegisterViewModel", "Erro durante o registro: ${e.message}", e)
            }
        }
    }

    // Funções auxiliares (mesmas que você já tinha)
    private fun calcularIdade(data: String): String {
        return try {
            val partes = data.split("/")
            val dia = partes[0].toInt()
            val mes = partes[1].toInt()
            val ano = partes[2].toInt()

            val dataNascimento = Calendar.getInstance().apply {
                set(ano, mes - 1, dia)
            }
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
}