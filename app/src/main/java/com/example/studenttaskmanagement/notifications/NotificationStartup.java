// NotificationStartup.java
package com.example.studenttaskmanagement.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class NotificationStartup {

    public static final String TASK_REMINDERS_CHANNEL_ID = "task_reminders";
    private static final String TASK_REMINDERS_CHANNEL_NAME = "Task reminders";
    private static final String TASK_REMINDERS_CHANNEL_DESCRIPTION =
            "Notifications for upcoming task reminders";

    private static final String TASK_REMINDER_WORK_NAME = "task_reminder_periodic_worker";

    private NotificationStartup() {}

    public static void initialize(@NonNull Context context) {
        Context appCtx = context.getApplicationContext();
        createNotificationChannel(appCtx);
        updateReminderWorkerSchedule(appCtx);
    }

    /** Call this after the user toggles reminders ON/OFF in Settings. */
    public static void updateReminderWorkerSchedule(@NonNull Context context) {
        Context appCtx = context.getApplicationContext();
        if (NotificationPreferences.areRemindersEnabled(appCtx)) {
            schedulePeriodicReminderWorker(appCtx);
        } else {
            cancelPeriodicReminderWorker(appCtx);
        }
    }

    private static void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                TASK_REMINDERS_CHANNEL_ID,
                TASK_REMINDERS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(TASK_REMINDERS_CHANNEL_DESCRIPTION);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);
    }

    private static void schedulePeriodicReminderWorker(@NonNull Context context) {
        // WorkManager minimum interval for PeriodicWork is 15 minutes.
        PeriodicWorkRequest reminderWork =
                new PeriodicWorkRequest.Builder(TaskReminderWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TASK_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // update if schedule/worker changes
                reminderWork
        );
    }

    private static void cancelPeriodicReminderWorker(@NonNull Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(TASK_REMINDER_WORK_NAME);
    }
}