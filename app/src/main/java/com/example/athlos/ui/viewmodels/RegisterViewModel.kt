// RegisterViewModel.kt
package com.example.athlos.ui.viewmodels // PACOTE CORRETO DA PASTA VIEWMODELS

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar // Importe Calendar
import com.google.android.gms.tasks.Task // Importe Task do GMS
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine // Importe suspendCoroutine

// Data class para representar o estado da UI da tela de registro
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

class RegisterViewModel : ViewModel() {

    // O estado da UI que será observado pelo Composable
    var uiState by mutableStateOf(RegisterUiState())
        private set

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Funções para atualizar o estado da UI
    fun updateNome(newNome: String) {
        uiState = uiState.copy(nome = newNome)
    }

    fun updateDataNascimento(newDateText: String) {
        val formatted = formatarData(newDateText)
        val calculatedAge = if (formatted.length == 10) calcularIdade(formatted) else ""
        uiState = uiState.copy(dataNascimentoText = formatted, idade = calculatedAge)
    }

    fun updateSexo(newSexo: String) {
        uiState = uiState.copy(sexo = newSexo, sexoExpanded = false)
    }

    fun toggleSexoDropdown() {
        uiState = uiState.copy(sexoExpanded = !uiState.sexoExpanded)
    }

    fun dismissSexoDropdown() {
        uiState = uiState.copy(sexoExpanded = false)
    }

    fun updatePeso(newPeso: String) {
        uiState = uiState.copy(peso = newPeso)
    }

    fun updateAltura(newAltura: String) {
        uiState = uiState.copy(altura = newAltura)
    }

    fun updateEmail(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun updateSenha(newSenha: String) {
        uiState = uiState.copy(senha = newSenha)
    }

    fun updatePraticaExercicios(newValue: Boolean) {
        uiState = uiState.copy(praticaExercicios = newValue)
    }

    fun updateDiasSemana(newDias: String) {
        uiState = uiState.copy(diasSemana = newDias)
    }

    // Lógica principal de registro
    fun registrarUsuario() {
        // Reinicia o estado de erro e carregamento
        uiState = uiState.copy(carregando = true, erroMensagem = null, registroSucesso = false)

        viewModelScope.launch {
            try {
                // 1. Criar usuário no Firebase Authentication
                val authResult = auth.createUserWithEmailAndPassword(uiState.email, uiState.senha).await()
                val uid = authResult.user?.uid

                if (uid != null) {
                    // 2. Salvar dados adicionais no Firestore
                    val userMap = mapOf(
                        "nome" to uiState.nome,
                        "dataNascimento" to uiState.dataNascimentoText,
                        "idade" to uiState.idade,
                        "sexo" to uiState.sexo,
                        "peso" to uiState.peso,
                        "altura" to uiState.altura,
                        "email" to uiState.email,
                        "praticaExercicios" to uiState.praticaExercicios,
                        "diasSemana" to uiState.diasSemana
                    )

                    firestore.collection("users").document(uid).set(userMap).await()

                    // Registro e salvamento de dados bem-sucedidos
                    uiState = uiState.copy(carregando = false, registroSucesso = true)
                    Log.d("RegisterViewModel", "Usuário registrado e dados salvos com sucesso!")

                } else {
                    uiState = uiState.copy(carregando = false, erroMensagem = "Erro: UID do usuário não encontrado após o cadastro.")
                    Log.e("RegisterViewModel", "Erro: UID do usuário é nulo após createUserWithEmailAndPassword.")
                }
            } catch (e: Exception) {
                // Trata erros de autenticação ou Firestore
                uiState = uiState.copy(carregando = false, erroMensagem = "Erro no cadastro: ${e.message}")
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

    // Função de extensão para converter Task em suspend function (Coroutines)
    // Coloque esta função aqui dentro do RegisterViewModel.kt
    private suspend fun <T> Task<T>.await(): T =
        suspendCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
}