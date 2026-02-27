package com.example.studenttaskmanagement.notifications;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.database.dao.TaskNotificationDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskNotification;

import java.util.Date;
import java.util.List;

public class TaskReminderWorker extends Worker {

    public TaskReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        long nowMillis = System.currentTimeMillis();

        TaskNotificationDao notificationDao = new TaskNotificationDao(context);
        TaskDao taskDao = new TaskDao(context);

        List<TaskNotification> pending = notificationDao.getPendingNotifications(nowMillis);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        for (TaskNotification reminder : pending) {
            Task task = taskDao.getTaskById(reminder.getTaskId());
            if (task == null) {
                notificationDao.markNotificationAsSent(reminder.getId());
                continue;
            }

            String title = task.getTitle() == null || task.getTitle().trim().isEmpty()
                    ? "Task reminder"
                    : task.getTitle().trim();
            String deadline = task.getDeadline() == null || task.getDeadline().trim().isEmpty()
                    ? "No deadline"
                    : task.getDeadline().trim();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationStartup.TASK_REMINDERS_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Upcoming task")
                    .setContentText(title + " • Deadline: " + deadline)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Task: " + title + "\nDeadline: " + deadline + "\nScheduled: " + new Date(reminder.getNotifyTimeMillis())))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify((int) reminder.getId(), builder.build());
            }

            notificationDao.markNotificationAsSent(reminder.getId());
        }

        return Result.success();
    }
}
