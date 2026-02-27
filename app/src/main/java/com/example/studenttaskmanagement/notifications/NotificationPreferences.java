package com.example.studenttaskmanagement.notifications;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class NotificationPreferences {

    public static final int LEAD_TIME_AT_DEADLINE = 0;
    public static final int LEAD_TIME_30_MIN = 30;
    public static final int LEAD_TIME_60_MIN = 60;

    private static final String PREF_FILE = "task_reminder_prefs";
    private static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    private static final String KEY_DEFAULT_LEAD_MINUTES = "default_lead_minutes";

    private NotificationPreferences() {
    }

    public static boolean areRemindersEnabled(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_REMINDERS_ENABLED, false);
    }

    public static void setRemindersEnabled(@NonNull Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply();
    }

    public static int getDefaultLeadTimeMinutes(@NonNull Context context) {
        return prefs(context).getInt(KEY_DEFAULT_LEAD_MINUTES, LEAD_TIME_30_MIN);
    }

    public static void setDefaultLeadTimeMinutes(@NonNull Context context, int leadMinutes) {
        prefs(context).edit().putInt(KEY_DEFAULT_LEAD_MINUTES, leadMinutes).apply();
    }

    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }
}
