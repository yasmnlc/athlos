package com.example.athlos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
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

    fun calcularIdade(data: String): String {
        return try {
            val partes = data.split("/")
            val ano = partes[2].toInt()
            val anoAtual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            "${anoAtual - ano} anos"
        } catch (e: Exception) {
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Cadastro",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            colors = defaultTextFieldColors()
        )

        OutlinedTextField(
            value = dataNascimentoState,
            onValueChange = {
                val formatted = formatarData(it.text)
                dataNascimentoState = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                if (formatted.length == 10) idade = calcularIdade(formatted)
            },
            label = { Text("Data de nascimento (dd/mm/aaaa)") },
            singleLine = true,
            colors = defaultTextFieldColors()
        )

        if (idade.isNotBlank()) Text("Idade: $idade", color = MaterialTheme.colorScheme.onBackground)

        ExposedDropdownMenuBox(
            expanded = sexoExpanded,
            onExpandedChange = { sexoExpanded = !sexoExpanded }
        ) {
            OutlinedTextField(
                value = sexo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sexo") },
                placeholder = { Text("Sexo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexoExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(0.75f)
                    .height(56.dp),
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

        OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso") }, colors = defaultTextFieldColors())
        OutlinedTextField(value = altura, onValueChange = { altura = it }, label = { Text("Altura") }, colors = defaultTextFieldColors())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, colors = defaultTextFieldColors())
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            colors = defaultTextFieldColors()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = praticaExercicios, onCheckedChange = { praticaExercicios = it })
            Text("Pratica exercícios?", color = MaterialTheme.colorScheme.onSurface)
        }

        if (praticaExercicios) {
            OutlinedTextField(
                value = diasSemana,
                onValueChange = { diasSemana = it },
                label = { Text("Quantos dias por semana?") },
                colors = defaultTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("main") { popUpTo("register") { inclusive = true } }
        }) {
            Text("Pular")
        }
    }
}

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
