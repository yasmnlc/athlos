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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.viewmodels.RegisterViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors
import com.example.athlos.ui.viewmodels.RegisterUiState
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val uiState by registerViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registroSucesso) {
        if (uiState.registroSucesso) {
            navController.navigate("main") {
                popUpTo("register") { inclusive = true }
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastro", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.nome,
            onValueChange = { registerViewModel.updateNome(it) },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var dataNascimentoTextFieldValue by remember(uiState.dataNascimentoText) {
            mutableStateOf(TextFieldValue(text = uiState.dataNascimentoText, selection = TextRange(uiState.dataNascimentoText.length)))
        }

        OutlinedTextField(
            value = dataNascimentoTextFieldValue,
            onValueChange = {
                dataNascimentoTextFieldValue = it
                registerViewModel.updateDataNascimento(it.text)
            },
            label = { Text("Data de nascimento (dd/mm/aaaa)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.idade.isNotBlank()) Text("Idade: ${uiState.idade}", color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = uiState.sexoExpanded,
            onExpandedChange = { registerViewModel.toggleSexoDropdown() }
        ) {
            OutlinedTextField(
                value = uiState.sexo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sexo") },
                placeholder = { Text("Sexo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.sexoExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = uiState.sexoExpanded,
                onDismissRequest = { registerViewModel.dismissSexoDropdown() }
            ) {
                val opcoesSexo = listOf("Masculino", "Feminino", "Outro")
                opcoesSexo.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { registerViewModel.updateSexo(option) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.peso,
            onValueChange = { registerViewModel.updatePeso(it) },
            label = { Text("Peso") },
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.altura,
            onValueChange = { registerViewModel.updateAltura(it) },
            label = { Text("Altura") },
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { registerViewModel.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.senha,
            onValueChange = { registerViewModel.updateSenha(it) },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.praticaExercicios,
                onCheckedChange = { registerViewModel.updatePraticaExercicios(it) }
            )
            Text("Pratica exercícios?", color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.praticaExercicios) {
            OutlinedTextField(
                value = uiState.diasSemana,
                onValueChange = { registerViewModel.updateDiasSemana(it) },
                label = { Text("Quantos dias por semana?") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.erroMensagem?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { registerViewModel.registrarUsuario() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.carregando,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.carregando) {
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