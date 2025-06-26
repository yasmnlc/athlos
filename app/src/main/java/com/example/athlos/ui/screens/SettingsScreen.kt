package com.example.athlos.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    val darkModeKey = booleanPreferencesKey("dark_mode")
    val animationsKey = booleanPreferencesKey("animations_enabled")
    val notificationsKey = booleanPreferencesKey("notifications_enabled")

    val darkMode = remember { mutableStateOf(false) }
    val animations = remember { mutableStateOf(false) }
    val notifications = remember { mutableStateOf(false) }

    // Carrega valores salvos
    LaunchedEffect(true) {
        val prefs = context.dataStore.data.first()
        darkMode.value = prefs[darkModeKey] ?: false
        animations.value = prefs[animationsKey] ?: false
        notifications.value = prefs[notificationsKey] ?: false
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Configurações", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        SwitchPreference(
            title = "Modo escuro",
            state = darkMode.value,
            onToggle = {
                darkMode.value = it
                savePref(context, darkModeKey, it)
            }
        )

        SwitchPreference(
            title = "Ativar animações",
            state = animations.value,
            onToggle = {
                animations.value = it
                savePref(context, animationsKey, it)
            }
        )

        SwitchPreference(
            title = "Ativar notificações",
            state = notifications.value,
            onToggle = {
                notifications.value = it
                savePref(context, notificationsKey, it)
            }
        )
    }
}

@Composable
fun SwitchPreference(title: String, state: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(checked = state, onCheckedChange = onToggle)
    }
}

fun savePref(context: Context, key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
    runBlocking {
        context.dataStore.edit { prefs ->
            prefs[key] = value
        }
    }
}
