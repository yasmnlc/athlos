package com.example.athlos.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Message
import com.example.athlos.notifications.NotificationScheduler
import kotlinx.coroutines.launch
import java.util.Calendar

val Context.dataStore by preferencesDataStore(name = "settings")
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")
val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val darkModeEnabled by context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }.collectAsState(initial = false)

    val animationsEnabled by context.dataStore.data.map { preferences ->
        preferences[ANIMATIONS_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val notificationsEnabled by context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    var notificationMessage by remember { mutableStateOf("") }
    var notificationHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var notificationMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleUserNotification(context, notificationMessage, notificationHour, notificationMinute)
        } else {
            Toast.makeText(context, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Modo Escuro")
            Switch(
                checked = darkModeEnabled,
                onCheckedChange = { isChecked ->
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[DARK_MODE_KEY] = isChecked
                        }
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Animações")
            Switch(
                checked = animationsEnabled,
                onCheckedChange = { isChecked ->
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[ANIMATIONS_ENABLED_KEY] = isChecked
                        }
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notificações Gerais")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[NOTIFICATIONS_ENABLED_KEY] = isChecked
                        }
                    }
                }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Agendar Notificação Personalizada",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = notificationMessage,
            onValueChange = { notificationMessage = it },
            label = { Text("Mensagem da Notificação") },
            leadingIcon = { Icon(Icons.Default.Message, contentDescription = "Mensagem") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    context,
                    { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                        notificationHour = selectedHour
                        notificationMinute = selectedMinute
                        Toast.makeText(context, "Horário selecionado: %02d:%02d".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
                    }, hour, minute, true
                )
                timePickerDialog.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Selecionar Horário")
            Spacer(Modifier.width(8.dp))
            Text("Selecionar Horário: %02d:%02d".format(notificationHour, notificationMinute))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (notificationMessage.isBlank()) {
                    Toast.makeText(context, "Por favor, insira uma mensagem para a notificação.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            scheduleUserNotification(context, notificationMessage, notificationHour, notificationMinute)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    scheduleUserNotification(context, notificationMessage, notificationHour, notificationMinute)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Agendar Notificação")
            Spacer(Modifier.width(8.dp))
            Text("Agendar Notificação")
        }
    }
}

private fun scheduleUserNotification(context: Context, message: String, hour: Int, minute: Int) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    NotificationScheduler.scheduleNotification(
        context,
        "Lembrete Athlos",
        message,
        calendar.timeInMillis
    )
    Toast.makeText(context, "Notificação agendada para %02d:%02d!".format(hour, minute), Toast.LENGTH_LONG).show()
}