package com.example.studenttaskmanagement.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studenttaskmanagement.database.AppDatabaseHelper;
import com.example.studenttaskmanagement.database.DatabaseContract;
import com.example.studenttaskmanagement.model.TaskNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CRUD operations on the notifications table.
 * v1 reminder model: one notification row per task.
 */
public class TaskNotificationDao {

    private final AppDatabaseHelper databaseHelper;

    public TaskNotificationDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    public long insertNotification(TaskNotification notification) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.insert(DatabaseContract.Notifications.TABLE_NAME, null, toContentValues(notification));
    }

    public TaskNotification getNotificationByTaskId(long taskId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.Notifications.TABLE_NAME,
                null,
                DatabaseContract.Notifications.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                DatabaseContract.Notifications._ID + " DESC",
                "1"
        );

        TaskNotification notification = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    notification = mapCursorToNotification(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return notification;
    }

    public long upsertNotificationForTask(long taskId, long notifyTimeMillis) {
        TaskNotification existing = getNotificationByTaskId(taskId);
        if (existing == null) {
            TaskNotification notification = new TaskNotification();
            notification.setTaskId(taskId);
            notification.setNotifyTimeMillis(notifyTimeMillis);
            notification.setIsSent(0);
            return insertNotification(notification);
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Notifications.COLUMN_NOTIFY_TIME, notifyTimeMillis);
        values.put(DatabaseContract.Notifications.COLUMN_IS_SENT, 0);

        int rows = db.update(
                DatabaseContract.Notifications.TABLE_NAME,
                values,
                DatabaseContract.Notifications.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
        return rows > 0 ? existing.getId() : -1L;
    }

    public int deleteNotificationByTaskId(long taskId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.Notifications.TABLE_NAME,
                DatabaseContract.Notifications.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public List<TaskNotification> getPendingNotifications(long nowMillis) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<TaskNotification> notifications = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseContract.Notifications.TABLE_NAME,
                null,
                DatabaseContract.Notifications.COLUMN_IS_SENT + " = 0 AND "
                        + DatabaseContract.Notifications.COLUMN_NOTIFY_TIME + " <= ?",
                new String[]{String.valueOf(nowMillis)},
                null,
                null,
                DatabaseContract.Notifications.COLUMN_NOTIFY_TIME + " ASC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    notifications.add(mapCursorToNotification(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return notifications;
    }

    public int markNotificationAsSent(long notificationId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Notifications.COLUMN_IS_SENT, 1);

        return db.update(
                DatabaseContract.Notifications.TABLE_NAME,
                values,
                DatabaseContract.Notifications._ID + " = ?",
                new String[]{String.valueOf(notificationId)}
        );
    }

    private ContentValues toContentValues(TaskNotification notification) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.Notifications.COLUMN_TASK_ID, notification.getTaskId());
        values.put(DatabaseContract.Notifications.COLUMN_NOTIFY_TIME, notification.getNotifyTimeMillis());
        values.put(DatabaseContract.Notifications.COLUMN_IS_SENT, notification.getIsSent());

        return values;
    }

    private TaskNotification mapCursorToNotification(Cursor cursor) {
        TaskNotification notification = new TaskNotification();

        int idIndex = cursor.getColumnIndex(DatabaseContract.Notifications._ID);
        int taskIdIndex = cursor.getColumnIndex(DatabaseContract.Notifications.COLUMN_TASK_ID);
        int notifyTimeIndex = cursor.getColumnIndex(DatabaseContract.Notifications.COLUMN_NOTIFY_TIME);
        int isSentIndex = cursor.getColumnIndex(DatabaseContract.Notifications.COLUMN_IS_SENT);

        if (idIndex >= 0 && !cursor.isNull(idIndex)) notification.setId(cursor.getLong(idIndex));
        if (taskIdIndex >= 0 && !cursor.isNull(taskIdIndex)) notification.setTaskId(cursor.getLong(taskIdIndex));
        if (notifyTimeIndex >= 0 && !cursor.isNull(notifyTimeIndex)) {
            notification.setNotifyTimeMillis(cursor.getLong(notifyTimeIndex));
        }
        if (isSentIndex >= 0 && !cursor.isNull(isSentIndex)) notification.setIsSent(cursor.getInt(isSentIndex));

        return notification;
    }
}
