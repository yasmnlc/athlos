// RegisterViewModel.kt
package com.example.athlos.ui.viewmodels // Pacote CORRETO para a pasta viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow // <-- Importe este
import kotlinx.coroutines.flow.StateFlow      // <-- Importe este
import kotlinx.coroutines.flow.asStateFlow    // <-- Importe este
import kotlinx.coroutines.launch
import java.util.Calendar // Garanta que esta importação esteja presente

// Data class para representar o estado da UI da tela de registro
data class RegisterUiState(
    val nome: String = "",
    val dataNascimentoText: String = "", // Texto formatado da data de nascimento
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
    val registroSucesso: Boolean = false // Novo estado para indicar sucesso no registro
)

class RegisterViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    // **MUDANÇA AQUI:** Use MutableStateFlow e o exponha como StateFlow
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Funções para atualizar o estado da UI
    fun updateNome(newNome: String) {
        // **MUDANÇA AQUI:** Atualizamos o valor do MutableStateFlow
        _uiState.value = _uiState.value.copy(nome = newNome)
    }

    fun updateDataNascimento(newDateText: String) {
        val formatted = formatarData(newDateText)
        val calculatedAge = if (formatted.length == 10) calcularIdade(formatted) else ""
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(dataNascimentoText = formatted, idade = calculatedAge)
    }

    fun updateSexo(newSexo: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(sexo = newSexo, sexoExpanded = false)
    }

    fun toggleSexoDropdown() {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(sexoExpanded = !_uiState.value.sexoExpanded)
    }

    fun dismissSexoDropdown() {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(sexoExpanded = false)
    }

    fun updatePeso(newPeso: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(peso = newPeso)
    }

    fun updateAltura(newAltura: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(altura = newAltura)
    }

    fun updateEmail(newEmail: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun updateSenha(newSenha: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(senha = newSenha)
    }

    fun updatePraticaExercicios(newValue: Boolean) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(praticaExercicios = newValue)
    }

    fun updateDiasSemana(newDias: String) {
        // **MUDANÇA AQUI:**
        _uiState.value = _uiState.value.copy(diasSemana = newDias)
    }

    // Lógica principal de registro
    fun registrarUsuario() {
        // Reinicia o estado de erro e carregamento
        // **MUDANÇA AQUI:**
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

                // Usa o AuthRepository para o registro
                authRepository.registerUser(_uiState.value.email, _uiState.value.senha, userDataMap)

                // Registro e salvamento de dados bem-sucedidos
                // **MUDANÇA AQUI:**
                _uiState.value = _uiState.value.copy(carregando = false, registroSucesso = true)
                Log.d("RegisterViewModel", "Usuário registrado e dados salvos com sucesso!")

            } catch (e: Exception) {
                // Trata erros de autenticação ou Firestore
                // **MUDANÇA AQUI:**
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
                set(ano, mes - 1, dia) // Mês é 0-indexed
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