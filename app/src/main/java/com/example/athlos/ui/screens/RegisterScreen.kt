package com.example.athlos.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar // Importe Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var nome by remember { mutableStateOf("") }
    var dataNascimentoState by remember { mutableStateOf(TextFieldValue("")) }
    var idade by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var praticaExercicios by remember { mutableStateOf(false) }
    var diasSemana by remember { mutableStateOf("") }
    var sexoExpanded by remember { mutableStateOf(false) }
    val opcoesSexo = listOf("Masculino", "Feminino", "Outro")
    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    fun calcularIdade(data: String): String {
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
            Log.e("RegisterScreen", "Erro ao calcular idade: ${e.message}")
            ""
        }
    }

    fun formatarData(input: String): String {
        val numbers = input.filter { it.isDigit() }.take(8)
        return buildString {
            for (i in numbers.indices) {
                append(numbers[i])
                if ((i == 1 || i == 3) && i != numbers.lastIndex) append('/')
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .imePadding() // Adiciona padding para o teclado virtual
            .verticalScroll(rememberScrollState()), // Adicionado para permitir scroll em telas menores
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastro", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp)) // Espaçamento entre campos

        OutlinedTextField(
            value = dataNascimentoState,
            onValueChange = {
                val formatted = formatarData(it.text)
                dataNascimentoState = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                if (formatted.length == 10) idade = calcularIdade(formatted)
            },
            label = { Text("Data de nascimento (dd/mm/aaaa)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (idade.isNotBlank()) Text("Idade: $idade", color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = sexoExpanded, onExpandedChange = { sexoExpanded = !sexoExpanded }) {
            OutlinedTextField(
                value = sexo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sexo") },
                placeholder = { Text("Sexo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexoExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            ExposedDropdownMenu(expanded = sexoExpanded, onDismissRequest = { sexoExpanded = false }) {
                opcoesSexo.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            sexo = option
                            sexoExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso") },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = altura,
            onValueChange = { altura = it },
            label = { Text("Altura") },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth() // Adicionado fillMaxWidth para a Row do Checkbox
        ) {
            Checkbox(checked = praticaExercicios, onCheckedChange = { praticaExercicios = it })
            Text("Pratica exercícios?", color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (praticaExercicios) {
            OutlinedTextField(
                value = diasSemana,
                onValueChange = { diasSemana = it },
                label = { Text("Quantos dias por semana?") },
                modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
                colors = defaultTextFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        erro?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall // Usa uma tipografia menor para erros
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                carregando = true
                erro = null

                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener { result ->
                        result.user?.uid?.let { uid ->
                            val userMap = mapOf(
                                "nome" to nome,
                                "dataNascimento" to dataNascimentoState.text,
                                "idade" to idade,
                                "sexo" to sexo,
                                "peso" to peso,
                                "altura" to altura,
                                "email" to email,
                                "praticaExercicios" to praticaExercicios,
                                "diasSemana" to diasSemana
                                // Adicione outros campos que você quiser salvar no Firestore aqui
                            )

                            firestore.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    carregando = false // DESATIVA O INDICADOR DE CARREGAMENTO AQUI!
                                    // A navegação só deve ocorrer após o Firebase e o Firestore estarem ok
                                    navController.navigate("main") {
                                        popUpTo("register") { inclusive = true }
                                        popUpTo("login") { inclusive = true } // Garante que a tela de login também seja removida, se presente
                                    }
                                }
                                .addOnFailureListener { firestoreError ->
                                    erro = "Erro ao salvar dados do usuário: ${firestoreError.message}"
                                    carregando = false // DESATIVA O INDICADOR TAMBÉM SE HOUVER ERRO NO FIRESTORE!
                                    Log.e("RegisterScreen", "Erro Firestore: ${firestoreError.message}", firestoreError)
                                }
                        } ?: run {
                            erro = "Erro: UID do usuário não encontrado após o cadastro."
                            carregando = false // DESATIVA SE O UID NÃO FOR ENCONTRADO
                            Log.e("RegisterScreen", "Erro: UID do usuário é nulo após createUserWithEmailAndPassword.")
                        }
                    }
                    .addOnFailureListener { authError ->
                        erro = "Erro no cadastro: ${authError.message}"
                        carregando = false // DESATIVA O INDICADOR EM CASO DE FALHA NA AUTENTICAÇÃO!
                        Log.e("RegisterScreen", "Erro Auth: ${authError.message}", authError)
                    }
            },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            enabled = !carregando, // Desabilita o botão durante o carregamento
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Cadastre-se")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
)