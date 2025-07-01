package com.example.athlos.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "settings")

// Boolean keys
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")
val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
val TRAINING_REMINDER_ENABLED_KEY = booleanPreferencesKey("training_reminder_enabled")
val WATER_REMINDER_ENABLED_KEY = booleanPreferencesKey("water_reminder_enabled")
val MEAL_BREAKFAST_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_breakfast_reminder_enabled")
val MEAL_LUNCH_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_lunch_reminder_enabled")
val MEAL_DINNER_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_dinner_reminder_enabled")
val MEAL_SNACKS_REMINDER_ENABLED_KEY = booleanPreferencesKey("meal_snacks_reminder_enabled")

// Integer keys
val MEAL_BREAKFAST_HOUR_KEY = intPreferencesKey("meal_breakfast_hour")
val MEAL_BREAKFAST_MINUTE_KEY = intPreferencesKey("meal_breakfast_minute")
val MEAL_LUNCH_HOUR_KEY = intPreferencesKey("meal_lunch_hour")
val MEAL_LUNCH_MINUTE_KEY = intPreferencesKey("meal_lunch_minute")
val MEAL_DINNER_HOUR_KEY = intPreferencesKey("meal_dinner_hour")
val MEAL_DINNER_MINUTE_KEY = intPreferencesKey("meal_dinner_minute")
val MEAL_SNACKS_HOUR_KEY = intPreferencesKey("meal_snacks_hour")
val MEAL_SNACKS_MINUTE_KEY = intPreferencesKey("meal_snacks_minute")
