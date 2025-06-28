package com.example.athlos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProfileScreen(mainNavController: NavHostController) { // Renomeado para mainNavController
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var meta by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.data
                        // Tenta carregar a meta se ela existir
                        meta = document.getString("meta") ?: ""
                    }
                    loading = false
                }
                .addOnFailureListener {
                    loading = false
                }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        userData?.let { data ->
            ProfileContent(data, meta, onMetaChange = { meta = it }, mainNavController) // Passa mainNavController
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Erro ao carregar dados.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ProfileContent(
    data: Map<String, Any>,
    meta: String,
    onMetaChange: (String) -> Unit,
    mainNavController: NavHostController // Renomeado para mainNavController
) {
    val nome = data["nome"] as? String ?: ""
    val idade = data["idade"] as? String ?: ""
    val sexo = data["sexo"] as? String ?: ""
    val peso = data["peso"] as? String ?: ""
    val altura = data["altura"] as? String ?: ""

    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var savingMeta by remember { mutableStateOf(false) } // Para mostrar um indicador de progresso ao salvar a meta

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Seu Perfil",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfo("Nome", nome)
                    ProfileInfo("Idade", idade)
                    ProfileInfo("Sexo", sexo)
                    ProfileInfo("Peso", peso)
                    ProfileInfo("Altura", altura)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Meta pessoal",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = meta,
                        onValueChange = onMetaChange,
                        placeholder = { Text("Ex: ganhar massa") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            savingMeta = true
                            user?.uid?.let { uid ->
                                firestore.collection("users").document(uid)
                                    .update("meta", meta) // Atualiza apenas o campo 'meta'
                                    .addOnSuccessListener {
                                        savingMeta = false
                                        // Opcional: mostrar um Toast de sucesso
                                    }
                                    .addOnFailureListener {
                                        savingMeta = false
                                        // Opcional: mostrar um Toast de erro
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !savingMeta // Desabilita o botão enquanto salva
                    ) {
                        if (savingMeta) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Salvar Meta")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            mainNavController.navigate("login") { // Usa mainNavController
                                popUpTo(mainNavController.graph.id) { // Limpa o back stack da navegação principal
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Sair")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
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