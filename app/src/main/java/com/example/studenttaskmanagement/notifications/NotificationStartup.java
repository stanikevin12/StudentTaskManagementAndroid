package com.example.studenttaskmanagement.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

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

    public static void initialize(Context context) {
        createNotificationChannel(context);
        updateReminderWorkerSchedule(context);
    }

    public static void updateReminderWorkerSchedule(Context context) {
        if (NotificationPreferences.areRemindersEnabled(context)) {
            schedulePeriodicReminderWorker(context);
        } else {
            cancelPeriodicReminderWorker(context);
        }
    }

    private static void createNotificationChannel(Context context) {
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

    private static void schedulePeriodicReminderWorker(Context context) {
        PeriodicWorkRequest reminderWork =
                new PeriodicWorkRequest.Builder(TaskReminderWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TASK_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderWork
        );
    }
}
