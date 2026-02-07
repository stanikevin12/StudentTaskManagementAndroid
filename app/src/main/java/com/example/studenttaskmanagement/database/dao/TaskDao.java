package com.example.studenttaskmanagement.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studenttaskmanagement.database.AppDatabaseHelper;
import com.example.studenttaskmanagement.database.DatabaseContract;
import com.example.studenttaskmanagement.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CRUD operations on the tasks table.
 */
public class TaskDao {

    private final AppDatabaseHelper databaseHelper;

    public TaskDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * Inserts a task row into the database.
     *
     * @param task Task data to insert.
     * @return row ID of the newly inserted task, or -1 if insertion failed.
     */
    public long insertTask(Task task) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = toContentValues(task, false);
        return db.insert(DatabaseContract.Tasks.TABLE_NAME, null, values);
    }

    /**
     * Returns a task by its ID.
     *
     * @param id task primary key.
     * @return Task object when found, otherwise null.
     */
    public Task getTaskById(long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.Tasks.TABLE_NAME,
                null,
                DatabaseContract.Tasks._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Task task = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    task = mapCursorToTask(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return task;
    }

    /**
     * Returns all tasks from the database ordered by ID descending.
     *
     * @return list of tasks, empty if no data exists.
     */
    public List<Task> getAllTasks() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<Task> taskList = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseContract.Tasks.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.Tasks._ID + " DESC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    taskList.add(mapCursorToTask(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return taskList;
    }

    /**
     * Updates an existing task row by ID.
     *
     * @param task task data with an existing ID.
     * @return number of affected rows.
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = toContentValues(task, false);

        return db.update(
                DatabaseContract.Tasks.TABLE_NAME,
                values,
                DatabaseContract.Tasks._ID + " = ?",
                new String[]{String.valueOf(task.getId())}
        );
    }

    /**
     * Deletes a task by ID.
     *
     * @param id task primary key.
     * @return number of affected rows.
     */
    public int deleteTask(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.Tasks.TABLE_NAME,
                DatabaseContract.Tasks._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private ContentValues toContentValues(Task task, boolean includeId) {
        ContentValues values = new ContentValues();

        if (includeId) {
            values.put(DatabaseContract.Tasks._ID, task.getId());
        }

        if (task.getTitle() != null) {
            values.put(DatabaseContract.Tasks.COLUMN_TITLE, task.getTitle());
        } else {
            values.putNull(DatabaseContract.Tasks.COLUMN_TITLE);
        }

        if (task.getDescription() != null) {
            values.put(DatabaseContract.Tasks.COLUMN_DESCRIPTION, task.getDescription());
        } else {
            values.putNull(DatabaseContract.Tasks.COLUMN_DESCRIPTION);
        }

        if (task.getDeadline() != null) {
            values.put(DatabaseContract.Tasks.COLUMN_DEADLINE, task.getDeadline());
        } else {
            values.putNull(DatabaseContract.Tasks.COLUMN_DEADLINE);
        }

        values.put(DatabaseContract.Tasks.COLUMN_STATUS, task.getStatus());
        values.put(DatabaseContract.Tasks.COLUMN_CATEGORY_ID, task.getCategoryId());
        values.put(DatabaseContract.Tasks.COLUMN_PRIORITY_ID, task.getPriorityId());
        values.put(DatabaseContract.Tasks.COLUMN_USER_ID, task.getUserId());

        return values;
    }

    private Task mapCursorToTask(Cursor cursor) {
        Task task = new Task();

        int idIndex = cursor.getColumnIndex(DatabaseContract.Tasks._ID);
        int titleIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_TITLE);
        int descriptionIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_DESCRIPTION);
        int deadlineIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_DEADLINE);
        int statusIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_STATUS);
        int categoryIdIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_CATEGORY_ID);
        int priorityIdIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_PRIORITY_ID);
        int userIdIndex = cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_USER_ID);

        if (idIndex >= 0 && !cursor.isNull(idIndex)) {
            task.setId((int) cursor.getLong(idIndex));
        }

        task.setTitle(titleIndex >= 0 && !cursor.isNull(titleIndex) ? cursor.getString(titleIndex) : null);
        task.setDescription(descriptionIndex >= 0 && !cursor.isNull(descriptionIndex)
                ? cursor.getString(descriptionIndex) : null);
        task.setDeadline(deadlineIndex >= 0 && !cursor.isNull(deadlineIndex)
                ? cursor.getString(deadlineIndex) : null);

        task.setStatus(statusIndex >= 0 && !cursor.isNull(statusIndex) ? cursor.getInt(statusIndex) : 0);
        task.setCategoryId(categoryIdIndex >= 0 && !cursor.isNull(categoryIdIndex)
                ? cursor.getInt(categoryIdIndex) : 0);
        task.setPriorityId(priorityIdIndex >= 0 && !cursor.isNull(priorityIdIndex)
                ? cursor.getInt(priorityIdIndex) : 0);
        task.setUserId(userIdIndex >= 0 && !cursor.isNull(userIdIndex)
                ? cursor.getInt(userIdIndex) : 0);

        return task;
    }
}
