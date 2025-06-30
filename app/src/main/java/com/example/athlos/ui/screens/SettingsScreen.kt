package com.example.athlos.ui.screens

import android.Manifest
import android.app.AlarmManager
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.athlos.NotificationScheduler
import kotlinx.coroutines.flow.first
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.runtime.snapshots.SnapshotStateList

val Context.dataStore by preferencesDataStore(name = "settings")
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")
val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
val TRAINING_REMINDER_ENABLED_KEY = booleanPreferencesKey("training_reminder_enabled")
val WATER_REMINDER_ENABLED_KEY = booleanPreferencesKey("water_reminder_enabled")

val MEAL_BREAKFAST_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_breakfast_reminder_enabled")
val MEAL_LUNCH_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_lunch_reminder_enabled")
val MEAL_DINNER_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_dinner_reminder_enabled")
val MEAL_SNACKS_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_snacks_reminder_enabled")

val MEAL_BREAKFAST_HOUR_KEY = intPreferencesKey("meal_breakfast_hour")
val MEAL_BREAKFAST_MINUTE_KEY = intPreferencesKey("meal_breakfast_minute")
val MEAL_LUNCH_HOUR_KEY = intPreferencesKey("meal_lunch_hour")
val MEAL_LUNCH_MINUTE_KEY = intPreferencesKey("meal_lunch_minute")
val MEAL_DINNER_HOUR_KEY = intPreferencesKey("meal_dinner_hour")
val MEAL_DINNER_MINUTE_KEY = intPreferencesKey("meal_dinner_minute")
val MEAL_SNACKS_HOUR_KEY = intPreferencesKey("meal_snacks_hour")
val MEAL_SNACKS_MINUTE_KEY = intPreferencesKey("meal_snacks_minute")

val CUSTOM_NOTIFICATIONS_KEY = stringPreferencesKey("custom_notifications")

data class CustomNotification(
    val id: Int,
    val message: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        NotificationScheduler.createNotificationChannel(context)
        onDispose {}
    }

    val darkModeEnabled by context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }.collectAsState(initial = false)

    val animationsEnabled by context.dataStore.data.map { preferences ->
        preferences[ANIMATIONS_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val notificationsEnabled by context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val trainingReminderEnabled by context.dataStore.data.map { preferences ->
        preferences[TRAINING_REMINDER_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val waterReminderEnabled by context.dataStore.data.map { preferences ->
        preferences[WATER_REMINDER_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val mealBreakfastReminderEnabled by context.dataStore.data.map { preferences ->
        preferences[MEAL_BREAKFAST_REMINDER_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val mealLunchReminderEnabled by context.dataStore.data.map { preferences ->
        preferences[MEAL_LUNCH_REMINDER_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val mealDinnerReminderEnabled by context.dataStore.data.map { preferences ->
        preferences[MEAL_DINNER_REMINDER_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val mealSnacksReminderEnabled by context.dataStore.data.map { preferences ->
        preferences[MEAL_SNACKS_REMINDER_ENABLED_KEY] ?: true
    }.collectAsState(initial = true)

    val currentCalendar = remember { Calendar.getInstance() }
    val defaultHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val defaultMinute = currentCalendar.get(Calendar.MINUTE)

    val breakfastHour by context.dataStore.data.map { it[MEAL_BREAKFAST_HOUR_KEY] ?: 8 }.collectAsState(initial = 8)
    val breakfastMinute by context.dataStore.data.map { it[MEAL_BREAKFAST_MINUTE_KEY] ?: 0 }.collectAsState(initial = 0)

    val lunchHour by context.dataStore.data.map { it[MEAL_LUNCH_HOUR_KEY] ?: 12 }.collectAsState(initial = 12)
    val lunchMinute by context.dataStore.data.map { it[MEAL_LUNCH_MINUTE_KEY] ?: 0 }.collectAsState(initial = 0)

    val dinnerHour by context.dataStore.data.map { it[MEAL_DINNER_HOUR_KEY] ?: 19 }.collectAsState(initial = 19)
    val dinnerMinute by context.dataStore.data.map { it[MEAL_DINNER_MINUTE_KEY] ?: 0 }.collectAsState(initial = 0)

    val snacksHour by context.dataStore.data.map { it[MEAL_SNACKS_HOUR_KEY] ?: 16 }.collectAsState(initial = 16)
    val snacksMinute by context.dataStore.data.map { it[MEAL_SNACKS_MINUTE_KEY] ?: 0 }.collectAsState(initial = 0)

    var customNotificationMessage by remember { mutableStateOf("") }
    var customNotificationHour by remember { mutableStateOf(defaultHour) }
    var customNotificationMinute by remember { mutableStateOf(defaultMinute) }
    var editingNotificationId by remember { mutableStateOf<Int?>(null) }

    val customNotifications = remember { mutableStateListOf<CustomNotification>() }
    LaunchedEffect(Unit) {
        context.dataStore.data.map { preferences ->
            val jsonString = preferences[CUSTOM_NOTIFICATIONS_KEY] ?: "[]"
            Gson().fromJson<List<CustomNotification>>(jsonString, object : TypeToken<List<CustomNotification>>() {}.type)
        }.collect { loadedList: List<CustomNotification> ->
            customNotifications.clear()
            customNotifications.addAll(loadedList)
        }
    }

    val saveCustomNotifications: (List<CustomNotification>) -> Unit = { list ->
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[CUSTOM_NOTIFICATIONS_KEY] = Gson().toJson(list)
            }
        }
    }

    val requestPostNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            handleScheduleCustomNotification(context, customNotifications, customNotificationMessage,
                customNotificationHour, customNotificationMinute, editingNotificationId, saveCustomNotifications)
            customNotificationMessage = ""
            customNotificationHour = defaultHour
            customNotificationMinute = defaultMinute
            editingNotificationId = null
        } else {
            Toast.makeText(context, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show()
        }
    }

    val requestExactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Permissão de alarme exato concedida.", Toast.LENGTH_SHORT).show()
                handleScheduleCustomNotification(context, customNotifications, customNotificationMessage,
                    customNotificationHour, customNotificationMinute, editingNotificationId, saveCustomNotifications)
                customNotificationMessage = ""
                customNotificationHour = defaultHour
                customNotificationMinute = defaultMinute
                editingNotificationId = null
            } else {
                Toast.makeText(context, "Permissão de alarme exato ainda não concedida.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Configurações",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Preferências",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider(modifier = Modifier.padding(bottom = 16.dp))

        PreferenceSwitchRow(
            title = "Modo Escuro",
            checked = darkModeEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[DARK_MODE_KEY] = isChecked
                    }
                }
            }
        )

        PreferenceSwitchRow(
            title = "Animações",
            checked = animationsEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[ANIMATIONS_ENABLED_KEY] = isChecked
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Notificações",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider(modifier = Modifier.padding(bottom = 16.dp))

        PreferenceSwitchRow(
            title = "Notificações Gerais",
            checked = notificationsEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[NOTIFICATIONS_ENABLED_KEY] = isChecked
                    }
                }
            }
        )

        PreferenceSwitchRow(
            title = "Lembrar de Treinar",
            checked = trainingReminderEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[TRAINING_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleUserNotification(context, "É hora do seu treino com o Athlos!", 18, 0, 1, "Lembrete de Treino Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, 1)
                }
                Toast.makeText(context, "Lembrete de Treino: ${if (isChecked) "Ativado" else "Desativado"}", Toast.LENGTH_SHORT).show()
            }
        )

        PreferenceSwitchRow(
            title = "Lembrar de Beber Água",
            checked = waterReminderEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[WATER_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleUserNotification(context, "Não se esqueça de se hidratar com o Athlos!", 8, 0, 2, "Lembrete de Água Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, 2)
                }
                Toast.makeText(context, "Lembrete de Beber Água: ${if (isChecked) "Ativado" else "Desativado"}", Toast.LENGTH_SHORT).show()
            }
        )

        Text(
            text = "Notificações de Refeição",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        MealNotificationSetting(
            title = "Café da Manhã",
            enabled = mealBreakfastReminderEnabled,
            hour = breakfastHour,
            minute = breakfastMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_BREAKFAST_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleUserNotification(context, "Hora do café da manhã com o Athlos!", breakfastHour, breakfastMinute, 3, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, 3)
                }
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_BREAKFAST_HOUR_KEY] = newHour
                        preferences[MEAL_BREAKFAST_MINUTE_KEY] = newMinute
                    }
                }
                if (mealBreakfastReminderEnabled) {
                    scheduleUserNotification(context, "Hora do café da manhã com o Athlos!", newHour, newMinute, 3, "Lembrete de Refeição Athlos")
                }
            }
        )

        MealNotificationSetting(
            title = "Almoço",
            enabled = mealLunchReminderEnabled,
            hour = lunchHour,
            minute = lunchMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_LUNCH_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleUserNotification(context, "Hora do almoço com o Athlos!", lunchHour, lunchMinute, 4, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, 4)
                }
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_LUNCH_HOUR_KEY] = newHour
                        preferences[MEAL_LUNCH_MINUTE_KEY] = newMinute
                    }
                }
                if (mealLunchReminderEnabled) {
                    scheduleUserNotification(context, "Hora do almoço com o Athlos!", newHour, newMinute, 4, "Lembrete de Refeição Athlos")
                }
            }
        )

        MealNotificationSetting(
            title = "Jantar",
            enabled = mealDinnerReminderEnabled,
            hour = dinnerHour,
            minute = dinnerMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_DINNER_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleUserNotification(context, "Hora do jantar com o Athlos!", dinnerHour, dinnerMinute, 5, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, 5)
                }
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_DINNER_HOUR_KEY] = newHour
                        preferences[MEAL_DINNER_MINUTE_KEY] = newMinute
                    }
                }
                if (mealDinnerReminderEnabled) {
                    scheduleUserNotification(context, "Hora do jantar com o Athlos!", newHour, newMinute, 5, "Lembrete de Refeição Athlos")
                }
            }
        )

        MealNotificationSetting(
            title = "Lanches/Outros",
            enabled = mealSnacksReminderEnabled,
            hour = snacksHour,
            minute = snacksMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_SNACKS_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleUserNotification(context, "Hora do lanche com o Athlos!", snacksHour, snacksMinute, 6, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, 6)
                }
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[MEAL_SNACKS_HOUR_KEY] = newHour
                        preferences[MEAL_SNACKS_MINUTE_KEY] = newMinute
                    }
                }
                if (mealSnacksReminderEnabled) {
                    scheduleUserNotification(context, "Hora do lanche com o Athlos!", newHour, newMinute, 6, "Lembrete de Refeição Athlos")
                }
            }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Agendar Notificação Personalizada",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = customNotificationMessage,
            onValueChange = { customNotificationMessage = it },
            label = { Text("Mensagem da Notificação") },
            leadingIcon = { Icons.Default.Message.let { Icon(it, contentDescription = "Mensagem") } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                        customNotificationHour = selectedHour
                        customNotificationMinute = selectedMinute
                        Toast.makeText(context, "Horário selecionado: %02d:%02d".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
                    }, customNotificationHour, customNotificationMinute, true
                )
                timePickerDialog.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Selecionar Horário")
            Spacer(Modifier.width(8.dp))
            Text("Selecionar Horário: %02d:%02d".format(customNotificationHour, customNotificationMinute))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (customNotificationMessage.isBlank()) {
                    Toast.makeText(context, "Por favor, insira uma mensagem para a notificação.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        requestExactAlarmPermissionLauncher.launch(intent)
                        Toast.makeText(context, "Por favor, conceda a permissão de 'Alarmes e Lembretes' para agendar notificações precisas.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            handleScheduleCustomNotification(context, customNotifications, customNotificationMessage,
                                customNotificationHour, customNotificationMinute, editingNotificationId, saveCustomNotifications)
                            customNotificationMessage = ""
                            customNotificationHour = defaultHour
                            customNotificationMinute = defaultMinute
                            editingNotificationId = null
                        }
                        else -> {
                            requestPostNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    handleScheduleCustomNotification(context, customNotifications, customNotificationMessage,
                        customNotificationHour, customNotificationMinute, editingNotificationId, saveCustomNotifications)
                    customNotificationMessage = ""
                    customNotificationHour = defaultHour
                    customNotificationMinute = defaultMinute
                    editingNotificationId = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Notificação")
            Spacer(Modifier.width(8.dp))
            Text(if (editingNotificationId != null) "Atualizar Notificação" else "Adicionar Notificação")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (customNotifications.isNotEmpty()) {
            Text(
                text = "Minhas Notificações Personalizadas",
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(modifier = Modifier.padding(bottom = 16.dp))

            val sortedCustomNotifications = remember(customNotifications) {
                customNotifications.sortedWith(compareBy<CustomNotification> { it.hour }.thenBy { it.minute })
            }

            sortedCustomNotifications.forEachIndexed { index, notification ->
                CustomNotificationItem(
                    notification = notification,
                    onEdit = {
                        customNotificationMessage = notification.message
                        customNotificationHour = notification.hour
                        customNotificationMinute = notification.minute
                        editingNotificationId = notification.id
                        val updatedList = customNotifications.toMutableList()
                        updatedList.removeAt(customNotifications.indexOf(notification))
                        saveCustomNotifications(updatedList)
                        NotificationScheduler.cancelNotification(context, notification.id)
                        Toast.makeText(context, "Notificação carregada para edição.", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        val updatedList = customNotifications.toMutableList()
                        updatedList.remove(notification)
                        saveCustomNotifications(updatedList)
                        NotificationScheduler.cancelNotification(context, notification.id)
                        Toast.makeText(context, "Notificação removida.", Toast.LENGTH_SHORT).show()
                    },
                    onToggleEnabled = { isChecked ->
                        val updatedList = customNotifications.toMutableList()
                        val updatedNotification = notification.copy(enabled = isChecked)
                        val idx = updatedList.indexOf(notification)
                        if (idx != -1) {
                            updatedList[idx] = updatedNotification
                        }
                        saveCustomNotifications(updatedList)

                        if (isChecked) {
                            scheduleUserNotification(context, updatedNotification.message,
                                updatedNotification.hour, updatedNotification.minute,
                                updatedNotification.id, "Lembrete Athlos Personalizado")
                            Toast.makeText(context, "Notificação ativada.", Toast.LENGTH_SHORT).show()
                        } else {
                            NotificationScheduler.cancelNotification(context, updatedNotification.id)
                            Toast.makeText(context, "Notificação desativada.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PreferenceSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun MealNotificationSetting(
    title: String,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Spacer(Modifier.height(4.dp))
            Text(
                text = String.format("Horário: %02d:%02d", hour, minute),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = enabled, onCheckedChange = onToggle)
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                        onTimeSelected(selectedHour, selectedMinute)
                        Toast.makeText(context, "Horário ${title} selecionado: %02d:%02d".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
                    }, hour, minute, true
                )
                timePickerDialog.show()
            },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Definir Horário")
        }
    }
}

@Composable
fun CustomNotificationItem(
    notification: CustomNotification,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (notification.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = String.format("Horário: %02d:%02d", notification.hour, notification.minute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notification.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Notificação")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover Notificação")
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = notification.enabled,
                    onCheckedChange = onToggleEnabled
                )
            }
        }
    }
}

private fun handleScheduleCustomNotification(
    context: Context,
    customNotifications: SnapshotStateList<CustomNotification>,
    message: String,
    hour: Int,
    minute: Int,
    editingId: Int?,
    saveCustomNotifications: (List<CustomNotification>) -> Unit
) {
    val idToUse = editingId ?: (NotificationScheduler.CUSTOM_NOTIFICATION_ID_BASE + System.currentTimeMillis().toInt() % 100000)
    val newNotification = CustomNotification(
        id = idToUse,
        message = message,
        hour = hour,
        minute = minute,
        enabled = true
    )

    if (editingId != null) {
        val index = customNotifications.indexOfFirst { it.id == editingId }
        if (index != -1) {
            customNotifications[index] = newNotification
        }
        Toast.makeText(context, "Notificação personalizada atualizada!", Toast.LENGTH_SHORT).show()
    } else {
        customNotifications.add(newNotification)
        Toast.makeText(context, "Notificação personalizada agendada!", Toast.LENGTH_SHORT).show()
    }

    saveCustomNotifications(customNotifications)

    scheduleUserNotification(context, newNotification.message, newNotification.hour,
        newNotification.minute, newNotification.id, "Lembrete Athlos Personalizado")
}

private fun scheduleUserNotification(context: Context, message: String, hour: Int, minute: Int, notificationId: Int, title: String) {
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
        title,
        message,
        calendar.timeInMillis,
        notificationId
    )
    Toast.makeText(context, "Notificação agendada para %02d:%02d!".format(hour, minute), Toast.LENGTH_LONG).show()
}