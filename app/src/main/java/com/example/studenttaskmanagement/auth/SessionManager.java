package com.example.studenttaskmanagement.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class SessionManager {

    private static final String PREF_FILE = "session_prefs";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";

    private final SharedPreferences preferences;

    public SessionManager(@NonNull Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    public void saveLoggedInUserId(long userId) {
        preferences.edit().putLong(KEY_LOGGED_IN_USER_ID, userId).apply();
    }

    public long getLoggedInUserId() {
        return preferences.getLong(KEY_LOGGED_IN_USER_ID, -1L);
    }

    public boolean isLoggedIn() {
        return getLoggedInUserId() > 0;
    }

    public void logout() {
        preferences.edit().remove(KEY_LOGGED_IN_USER_ID).apply();
    }
}
