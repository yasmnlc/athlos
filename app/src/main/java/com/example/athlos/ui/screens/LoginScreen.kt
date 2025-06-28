package com.example.athlos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class) // Anotação necessária para OutlinedTextFieldDefaults.colors
@Composable
fun LoginScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp) // Adicionado padding horizontal para melhor visual
            .imePadding(), // Garante que o conteúdo role para cima com o teclado virtual
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Bem-vindo de volta!",
            fontSize = 28.sp, // Tamanho um pouco maior para o título
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge // Usa a tipografia do tema
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors(),
            singleLine = true // Adicionado para melhor UX
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            colors = defaultTextFieldColors(),
            singleLine = true // Adicionado para melhor UX
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exibe o erro se houver
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
                erro = null // Limpa o erro anterior ao tentar novo login
                carregando = true

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        carregando = false
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true } // Remove a tela de login da pilha
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Mensagens de erro mais amigáveis
                        erro = when (exception) {
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Usuário não encontrado. Verifique seu email."
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Senha incorreta. Tente novamente."
                            else -> "Erro no login: ${exception.message}"
                        }
                        carregando = false
                    }
            },
            modifier = Modifier.fillMaxWidth(), // Adicionado fillMaxWidth
            enabled = !carregando, // Desabilita o botão enquanto carrega
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium // Aplica o shape padrão
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), // Tamanho menor para o indicador
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp // Espessura menor
                )
            } else {
                Text("Entrar")
            }
        }


        Spacer(modifier = Modifier.height(16.dp)) // Aumentado o espaçamento

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth() // Adicionado fillMaxWidth para o TextButton
        ) {
            Text("Não tem uma conta? Cadastre-se aqui.", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Anotação necessária para OutlinedTextFieldDefaults.colors
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