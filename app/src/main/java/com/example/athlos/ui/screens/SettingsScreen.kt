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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.text.SimpleDateFormat
import java.util.*

// DataStore and Keys
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

// Notification IDs
private const val WATER_REMINDER_ID = 2
private const val MEAL_BREAKFAST_ID = 3
private const val MEAL_LUNCH_ID = 4
private const val MEAL_DINNER_ID = 5
private const val MEAL_SNACKS_ID = 6
private const val TRAINING_REMINDER_ID_7AM = 101
private const val TRAINING_REMINDER_ID_12PM = 102
private const val TRAINING_REMINDER_ID_3PM = 103
private const val TRAINING_REMINDER_ID_6PM = 104

data class CustomNotification(
    val id: Int,
    val message: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Set<Int> = emptySet(),
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

    // State collection from DataStore
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

    // State for custom notifications
    var customNotificationMessage by remember { mutableStateOf("") }
    var customNotificationHour by remember { mutableStateOf(defaultHour) }
    var customNotificationMinute by remember { mutableStateOf(defaultMinute) }
    var editingNotificationId by remember { mutableStateOf<Int?>(null) }
    var selectedDays by remember { mutableStateOf<Set<Int>>(emptySet()) }


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
                customNotificationHour, customNotificationMinute, selectedDays, editingNotificationId, saveCustomNotifications)
            customNotificationMessage = ""
            customNotificationHour = defaultHour
            customNotificationMinute = defaultMinute
            selectedDays = emptySet()
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
                    customNotificationHour, customNotificationMinute, selectedDays, editingNotificationId, saveCustomNotifications)
                customNotificationMessage = ""
                customNotificationHour = defaultHour
                customNotificationMinute = defaultMinute
                selectedDays = emptySet()
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
                        if (!isChecked) {
                            preferences[TRAINING_REMINDER_ENABLED_KEY] = false
                            preferences[WATER_REMINDER_ENABLED_KEY] = false
                            preferences[MEAL_BREAKFAST_REMINDER_ENABLED_KEY] = false
                            preferences[MEAL_LUNCH_REMINDER_ENABLED_KEY] = false
                            preferences[MEAL_DINNER_REMINDER_ENABLED_KEY] = false
                            preferences[MEAL_SNACKS_REMINDER_ENABLED_KEY] = false

                            NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_7AM)
                            NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_12PM)
                            NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_3PM)
                            NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_6PM)
                            NotificationScheduler.cancelNotification(context, WATER_REMINDER_ID)
                            NotificationScheduler.cancelNotification(context, MEAL_BREAKFAST_ID)
                            NotificationScheduler.cancelNotification(context, MEAL_LUNCH_ID)
                            NotificationScheduler.cancelNotification(context, MEAL_DINNER_ID)
                            NotificationScheduler.cancelNotification(context, MEAL_SNACKS_ID)

                            val jsonString = preferences[CUSTOM_NOTIFICATIONS_KEY] ?: "[]"
                            val listType = object : TypeToken<List<CustomNotification>>() {}.type
                            val currentList = Gson().fromJson<List<CustomNotification>>(jsonString, listType)
                            val updatedList = currentList.map { it.copy(enabled = false) }
                            updatedList.forEach { notification ->
                                NotificationScheduler.cancelNotification(context, notification.id)
                            }
                            preferences[CUSTOM_NOTIFICATIONS_KEY] = Gson().toJson(updatedList)
                        }
                    }
                    if (!isChecked) {
                        Toast.makeText(context, "Todas as notificações foram desativadas.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        PreferenceSwitchRow(
            title = "Lembrar de Treinar",
            checked = trainingReminderEnabled,
            enabled = notificationsEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[TRAINING_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    val message = "Já foi treinar hoje? No pain no gain, brother"
                    val title = "Lembrete de Treino Athlos"
                    scheduleDailyNotification(context, message, 7, 0, TRAINING_REMINDER_ID_7AM, title)
                    scheduleDailyNotification(context, message, 12, 0, TRAINING_REMINDER_ID_12PM, title)
                    scheduleDailyNotification(context, message, 15, 0, TRAINING_REMINDER_ID_3PM, title)
                    scheduleDailyNotification(context, message, 18, 0, TRAINING_REMINDER_ID_6PM, title)
                    Toast.makeText(context, "Lembretes de treino ativados para 07:00, 12:00, 15:00 e 18:00.", Toast.LENGTH_LONG).show()
                } else {
                    NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_7AM)
                    NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_12PM)
                    NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_3PM)
                    NotificationScheduler.cancelNotification(context, TRAINING_REMINDER_ID_6PM)
                    Toast.makeText(context, "Lembretes de treino desativados.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        PreferenceSwitchRow(
            title = "Lembrar de Beber Água",
            checked = waterReminderEnabled,
            enabled = notificationsEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[WATER_REMINDER_ENABLED_KEY] = isChecked
                    }
                }
                if (isChecked) {
                    scheduleDailyNotification(context, "Não se esqueça de se hidratar com o Athlos!", 8, 0, WATER_REMINDER_ID, "Lembrete de Água Athlos")
                    Toast.makeText(context, "Lembrete de Beber Água Ativado", Toast.LENGTH_SHORT).show()
                } else {
                    NotificationScheduler.cancelNotification(context, WATER_REMINDER_ID)
                    Toast.makeText(context, "Lembrete de Beber Água Desativado", Toast.LENGTH_SHORT).show()
                }
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
            masterSwitchEnabled = notificationsEnabled,
            hour = breakfastHour,
            minute = breakfastMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { p -> p[MEAL_BREAKFAST_REMINDER_ENABLED_KEY] = isChecked }
                }
                if (isChecked) {
                    scheduleDailyNotification(context, "Hora do café da manhã com o Athlos!", breakfastHour, breakfastMinute, MEAL_BREAKFAST_ID, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, MEAL_BREAKFAST_ID)
                }
                Toast.makeText(context, "Lembrete de Café da Manhã ${if (isChecked) "ativado" else "desativado"}", Toast.LENGTH_SHORT).show()
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { p ->
                        p[MEAL_BREAKFAST_HOUR_KEY] = newHour
                        p[MEAL_BREAKFAST_MINUTE_KEY] = newMinute
                    }
                }
                if (mealBreakfastReminderEnabled) {
                    scheduleDailyNotification(context, "Hora do café da manhã com o Athlos!", newHour, newMinute, MEAL_BREAKFAST_ID, "Lembrete de Refeição Athlos")
                }
                Toast.makeText(context, "Horário do Café da Manhã atualizado para %02d:%02d".format(newHour, newMinute), Toast.LENGTH_SHORT).show()
            }
        )

        MealNotificationSetting(
            title = "Almoço",
            enabled = mealLunchReminderEnabled,
            masterSwitchEnabled = notificationsEnabled,
            hour = lunchHour,
            minute = lunchMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { p -> p[MEAL_LUNCH_REMINDER_ENABLED_KEY] = isChecked }
                }
                if (isChecked) {
                    scheduleDailyNotification(context, "Hora do almoço com o Athlos!", lunchHour, lunchMinute, MEAL_LUNCH_ID, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, MEAL_LUNCH_ID)
                }
                Toast.makeText(context, "Lembrete de Almoço ${if (isChecked) "ativado" else "desativado"}", Toast.LENGTH_SHORT).show()
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { p ->
                        p[MEAL_LUNCH_HOUR_KEY] = newHour
                        p[MEAL_LUNCH_MINUTE_KEY] = newMinute
                    }
                }
                if (mealLunchReminderEnabled) {
                    scheduleDailyNotification(context, "Hora do almoço com o Athlos!", newHour, newMinute, MEAL_LUNCH_ID, "Lembrete de Refeição Athlos")
                }
                Toast.makeText(context, "Horário do Almoço atualizado para %02d:%02d".format(newHour, newMinute), Toast.LENGTH_SHORT).show()
            }
        )

        MealNotificationSetting(
            title = "Jantar",
            enabled = mealDinnerReminderEnabled,
            masterSwitchEnabled = notificationsEnabled,
            hour = dinnerHour,
            minute = dinnerMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { p -> p[MEAL_DINNER_REMINDER_ENABLED_KEY] = isChecked }
                }
                if (isChecked) {
                    scheduleDailyNotification(context, "Hora do jantar com o Athlos!", dinnerHour, dinnerMinute, MEAL_DINNER_ID, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, MEAL_DINNER_ID)
                }
                Toast.makeText(context, "Lembrete de Jantar ${if (isChecked) "ativado" else "desativado"}", Toast.LENGTH_SHORT).show()
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { p ->
                        p[MEAL_DINNER_HOUR_KEY] = newHour
                        p[MEAL_DINNER_MINUTE_KEY] = newMinute
                    }
                }
                if (mealDinnerReminderEnabled) {
                    scheduleDailyNotification(context, "Hora do jantar com o Athlos!", newHour, newMinute, MEAL_DINNER_ID, "Lembrete de Refeição Athlos")
                }
                Toast.makeText(context, "Horário do Jantar atualizado para %02d:%02d".format(newHour, newMinute), Toast.LENGTH_SHORT).show()
            }
        )

        MealNotificationSetting(
            title = "Lanches/Outros",
            enabled = mealSnacksReminderEnabled,
            masterSwitchEnabled = notificationsEnabled,
            hour = snacksHour,
            minute = snacksMinute,
            onToggle = { isChecked ->
                scope.launch {
                    context.dataStore.edit { p -> p[MEAL_SNACKS_REMINDER_ENABLED_KEY] = isChecked }
                }
                if (isChecked) {
                    scheduleDailyNotification(context, "Hora do lanche com o Athlos!", snacksHour, snacksMinute, MEAL_SNACKS_ID, "Lembrete de Refeição Athlos")
                } else {
                    NotificationScheduler.cancelNotification(context, MEAL_SNACKS_ID)
                }
                Toast.makeText(context, "Lembrete de Lanche ${if (isChecked) "ativado" else "desativado"}", Toast.LENGTH_SHORT).show()
            },
            onTimeSelected = { newHour, newMinute ->
                scope.launch {
                    context.dataStore.edit { p ->
                        p[MEAL_SNACKS_HOUR_KEY] = newHour
                        p[MEAL_SNACKS_MINUTE_KEY] = newMinute
                    }
                }
                if (mealSnacksReminderEnabled) {
                    scheduleDailyNotification(context, "Hora do lanche com o Athlos!", newHour, newMinute, MEAL_SNACKS_ID, "Lembrete de Refeição Athlos")
                }
                Toast.makeText(context, "Horário do Lanche atualizado para %02d:%02d".format(newHour, newMinute), Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.fillMaxWidth(),
            enabled = notificationsEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, selectedHour: Int, selectedMinute: Int ->
                        customNotificationHour = selectedHour
                        customNotificationMinute = selectedMinute
                        Toast.makeText(context, "Horário selecionado: %02d:%02d".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
                    }, customNotificationHour, customNotificationMinute, true
                )
                timePickerDialog.show()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = notificationsEnabled
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Selecionar Horário")
            Spacer(Modifier.width(8.dp))
            Text("Selecionar Horário: %02d:%02d".format(customNotificationHour, customNotificationMinute))
        }

        Spacer(modifier = Modifier.height(16.dp))

        DayOfWeekSelector(
            selectedDays = selectedDays,
            onDaySelected = { day ->
                selectedDays = if (selectedDays.contains(day)) {
                    selectedDays - day
                } else {
                    selectedDays + day
                }
            },
            enabled = notificationsEnabled
        )

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
                                customNotificationHour, customNotificationMinute, selectedDays, editingNotificationId, saveCustomNotifications)
                            customNotificationMessage = ""
                            customNotificationHour = defaultHour
                            customNotificationMinute = defaultMinute
                            selectedDays = emptySet()
                            editingNotificationId = null
                        }
                        else -> {
                            requestPostNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    handleScheduleCustomNotification(context, customNotifications, customNotificationMessage,
                        customNotificationHour, customNotificationMinute, selectedDays, editingNotificationId, saveCustomNotifications)
                    customNotificationMessage = ""
                    customNotificationHour = defaultHour
                    customNotificationMinute = defaultMinute
                    selectedDays = emptySet()
                    editingNotificationId = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = notificationsEnabled,
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

            sortedCustomNotifications.forEach { notification ->
                CustomNotificationItem(
                    notification = notification,
                    masterSwitchEnabled = notificationsEnabled,
                    onEdit = {
                        customNotificationMessage = notification.message
                        customNotificationHour = notification.hour
                        customNotificationMinute = notification.minute
                        selectedDays = notification.daysOfWeek
                        editingNotificationId = notification.id
                        // Remove the old notification before editing
                        val updatedList = customNotifications.toMutableList()
                        updatedList.remove(notification)
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
                            scheduleCustomNotification(context, updatedNotification)
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
fun PreferenceSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun MealNotificationSetting(
    title: String,
    enabled: Boolean,
    masterSwitchEnabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val itemIsEnabled = enabled && masterSwitchEnabled
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (masterSwitchEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = String.format("Horário: %02d:%02d", hour, minute),
                style = MaterialTheme.typography.bodySmall,
                color = if (masterSwitchEnabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = enabled, onCheckedChange = onToggle, enabled = masterSwitchEnabled)
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, selectedHour: Int, selectedMinute: Int ->
                        onTimeSelected(selectedHour, selectedMinute)
                    }, hour, minute, true
                )
                timePickerDialog.show()
            },
            enabled = itemIsEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Definir Horário")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDaySelected: (Int) -> Unit,
    enabled: Boolean
) {
    val days = listOf(
        "D" to Calendar.SUNDAY,
        "S" to Calendar.MONDAY,
        "T" to Calendar.TUESDAY,
        "Q" to Calendar.WEDNESDAY,
        "Q" to Calendar.THURSDAY,
        "S" to Calendar.FRIDAY,
        "S" to Calendar.SATURDAY
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(days.withIndex().toList()) { (index, dayInfo) ->
            val (label, day) = dayInfo
            val uniqueLabel = when(index) {
                3 -> "Qua"
                4 -> "Qui"
                5 -> "Sex"
                6 -> "Sáb"
                0 -> "Dom"
                1 -> "Seg"
                2 -> "Ter"
                else -> label
            }
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = { onDaySelected(day) },
                label = { Text(uniqueLabel) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun CustomNotificationItem(
    notification: CustomNotification,
    masterSwitchEnabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    val isEnabled = notification.enabled && masterSwitchEnabled
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format("Horário: %02d:%02d", notification.hour, notification.minute),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, enabled = masterSwitchEnabled) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Notificação")
                    }
                    IconButton(onClick = onDelete, enabled = masterSwitchEnabled) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover Notificação")
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = notification.enabled,
                        onCheckedChange = onToggleEnabled,
                        enabled = masterSwitchEnabled
                    )
                }
            }
            if (notification.daysOfWeek.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Dias: ${formatSelectedDays(notification.daysOfWeek)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
    daysOfWeek: Set<Int>,
    editingId: Int?,
    saveCustomNotifications: (List<CustomNotification>) -> Unit
) {
    val idToUse = editingId ?: (NotificationScheduler.CUSTOM_NOTIFICATION_ID_BASE + System.currentTimeMillis().toInt() % 100000)
    val newNotification = CustomNotification(
        id = idToUse,
        message = message,
        hour = hour,
        minute = minute,
        daysOfWeek = daysOfWeek,
        enabled = true
    )

    if (editingId != null) {
        val index = customNotifications.indexOfFirst { it.id == editingId }
        if (index != -1) {
            customNotifications[index] = newNotification
        } else {
            customNotifications.add(newNotification)
        }
        Toast.makeText(context, "Notificação personalizada atualizada!", Toast.LENGTH_SHORT).show()
    } else {
        customNotifications.add(newNotification)
        Toast.makeText(context, "Notificação personalizada agendada!", Toast.LENGTH_SHORT).show()
    }

    saveCustomNotifications(customNotifications.toList())
    scheduleCustomNotification(context, newNotification)
}

private fun scheduleCustomNotification(context: Context, notification: CustomNotification) {
    val title = "Lembrete Athlos Personalizado"

    if (notification.daysOfWeek.isEmpty()) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notification.hour)
            set(Calendar.MINUTE, notification.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        NotificationScheduler.scheduleNotification(
            context,
            title,
            notification.message,
            calendar.timeInMillis,
            notification.id
        )
        Toast.makeText(context, "Notificação agendada para %02d:%02d!".format(notification.hour, notification.minute), Toast.LENGTH_LONG).show()
    } else {
        notification.daysOfWeek.forEach { dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, notification.hour)
                set(Calendar.MINUTE, notification.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val dailyNotificationId = notification.id + dayOfWeek

            NotificationScheduler.scheduleRepeatingNotification(
                context,
                title,
                notification.message,
                calendar.timeInMillis,
                dailyNotificationId
            )
        }
        Toast.makeText(context, "Notificações recorrentes agendadas!", Toast.LENGTH_LONG).show()
    }
}

private fun scheduleDailyNotification(context: Context, message: String, hour: Int, minute: Int, notificationId: Int, title: String) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    NotificationScheduler.scheduleRepeatingNotification(
        context,
        title,
        message,
        calendar.timeInMillis,
        notificationId
    )
}

private fun formatSelectedDays(days: Set<Int>): String {
    if (days.isEmpty()) return "Nenhum"
    val sortedDays = days.sorted()
    val locale = Locale("pt", "BR")
    return sortedDays.joinToString(", ") { day ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, day)
        SimpleDateFormat("EEE", locale).format(cal.time).replaceFirstChar { it.uppercase() }
    }
}