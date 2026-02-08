package com.example.studenttaskmanagement.utils;

import java.util.Locale;

/**
 * Small utility for formatting durations.
 */
public final class DurationUtils {

    private DurationUtils() {
        // Utility class
    }

    public static String formatMinutes(long durationMillis) {
        long minutes = Math.max(0L, durationMillis / 60000L);
        return String.format(Locale.getDefault(), "%d min", minutes);
    }
}
