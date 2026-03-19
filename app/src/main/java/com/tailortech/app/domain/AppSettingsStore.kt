package com.tailortech.app.domain

import android.content.Context

data class AppSettings(
    val remindersEnabled: Boolean = false,
    val reminderIntervalDays: Int = 14,
    val lastMeasurementEpochMs: Long = 0L,
    val geminiApiKeyOverride: String = ""
)

class AppSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AppSettings {
        return AppSettings(
            remindersEnabled = prefs.getBoolean(KEY_REMINDERS_ENABLED, false),
            reminderIntervalDays = prefs.getInt(KEY_REMINDER_DAYS, 14),
            lastMeasurementEpochMs = prefs.getLong(KEY_LAST_MEASURED_MS, 0L),
            geminiApiKeyOverride = prefs.getString(KEY_GEMINI_OVERRIDE, "").orEmpty()
        )
    }

    fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean(KEY_REMINDERS_ENABLED, settings.remindersEnabled)
            .putInt(KEY_REMINDER_DAYS, settings.reminderIntervalDays)
            .putLong(KEY_LAST_MEASURED_MS, settings.lastMeasurementEpochMs)
            .putString(KEY_GEMINI_OVERRIDE, settings.geminiApiKeyOverride)
            .apply()
    }

    fun updateLastMeasurementNow() {
        prefs.edit()
            .putLong(KEY_LAST_MEASURED_MS, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "tailortech_settings"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_DAYS = "reminder_days"
        private const val KEY_LAST_MEASURED_MS = "last_measured_ms"
        private const val KEY_GEMINI_OVERRIDE = "gemini_override"
    }
}
