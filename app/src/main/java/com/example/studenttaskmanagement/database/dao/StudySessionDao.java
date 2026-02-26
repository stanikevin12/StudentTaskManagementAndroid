package com.example.studenttaskmanagement.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studenttaskmanagement.database.AppDatabaseHelper;
import com.example.studenttaskmanagement.database.DatabaseContract;
import com.example.studenttaskmanagement.model.StudySession;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for study session operations.
 *
 * Updated to match schema where:
 * - start_time and end_time are INTEGER (epoch millis)
 * - task_id has FK ON DELETE CASCADE (handled in schema)
 */
public class StudySessionDao {

    private final AppDatabaseHelper databaseHelper;

    public StudySessionDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * Starts a study session for a task by inserting start time.
     * End time is NULL until the session is ended.
     *
     * @param taskId    task ID associated with the session.
     * @param startTime session start time in epoch milliseconds.
     * @return row ID of the inserted session, or -1 if insertion failed.
     */
    public long startSession(long taskId, long startTime) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.StudySessions.COLUMN_TASK_ID, taskId);

        // INTEGER columns (epoch millis)
        values.put(DatabaseContract.StudySessions.COLUMN_START_TIME, startTime);
        values.putNull(DatabaseContract.StudySessions.COLUMN_END_TIME);

        // duration in millis (INTEGER)
        values.put(DatabaseContract.StudySessions.COLUMN_DURATION, 0L);

        return db.insert(DatabaseContract.StudySessions.TABLE_NAME, null, values);
    }

    /**
     * Ends an existing study session by writing end time and calculated duration.
     * Duration is computed as (endTime - startTime) and bounded to be non-negative.
     *
     * @param sessionId study session ID.
     * @param endTime   session end time in epoch milliseconds.
     * @return number of affected rows.
     */
    public int endSession(long sessionId, long endTime) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Long startTime = getSessionStartTime(db, sessionId);
        if (startTime == null) {
            return 0;
        }

        long duration = Math.max(0L, endTime - startTime);

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.StudySessions.COLUMN_END_TIME, endTime);
        values.put(DatabaseContract.StudySessions.COLUMN_DURATION, duration);

        return db.update(
                DatabaseContract.StudySessions.TABLE_NAME,
                values,
                DatabaseContract.StudySessions._ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    /**
     * Returns a single study session by its ID.
     *
     * @param sessionId session primary key.
     * @return StudySession if found, otherwise null.
     */
    public StudySession getSessionById(long sessionId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.StudySessions.TABLE_NAME,
                null,
                DatabaseContract.StudySessions._ID + " = ?",
                new String[]{String.valueOf(sessionId)},
                null,
                null,
                null
        );

        if (cursor == null) return null;

        try {
            if (!cursor.moveToFirst()) return null;
            return mapCursorToStudySession(cursor);
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all study sessions for a given task, newest first.
     *
     * @param taskId task ID.
     * @return list of StudySession records, empty if none exist.
     */
    public List<StudySession> getSessionsForTask(long taskId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<StudySession> sessions = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseContract.StudySessions.TABLE_NAME,
                null,
                DatabaseContract.StudySessions.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                DatabaseContract.StudySessions.COLUMN_START_TIME + " DESC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    sessions.add(mapCursorToStudySession(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return sessions;
    }


    /**
     * Returns how many completed study sessions exist for a task.
     * A completed session is a row with end_time > 0.
     */
    public int getCompletedSessionCountForTask(long taskId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseContract.StudySessions.TABLE_NAME
                        + " WHERE " + DatabaseContract.StudySessions.COLUMN_TASK_ID + " = ?"
                        + " AND " + DatabaseContract.StudySessions.COLUMN_END_TIME + " > 0",
                new String[]{String.valueOf(taskId)}
        );

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Deletes a single study session by ID.
     */
    public int deleteSessionById(long sessionId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.StudySessions.TABLE_NAME,
                DatabaseContract.StudySessions._ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    /**
     * Deletes all sessions for a given task.
     * (Optional helper; with ON DELETE CASCADE, deleting the task also deletes sessions.)
     */
    public int deleteSessionsForTask(long taskId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.StudySessions.TABLE_NAME,
                DatabaseContract.StudySessions.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    private Long getSessionStartTime(SQLiteDatabase db, long sessionId) {
        Cursor cursor = db.query(
                DatabaseContract.StudySessions.TABLE_NAME,
                new String[]{DatabaseContract.StudySessions.COLUMN_START_TIME},
                DatabaseContract.StudySessions._ID + " = ?",
                new String[]{String.valueOf(sessionId)},
                null,
                null,
                null
        );

        if (cursor == null) return null;

        try {
            if (!cursor.moveToFirst()) return null;

            int startTimeIndex = cursor.getColumnIndex(DatabaseContract.StudySessions.COLUMN_START_TIME);
            if (startTimeIndex < 0 || cursor.isNull(startTimeIndex)) return null;

            // INTEGER epoch millis
            return cursor.getLong(startTimeIndex);
        } finally {
            cursor.close();
        }
    }

    private StudySession mapCursorToStudySession(Cursor cursor) {
        StudySession session = new StudySession();

        int idIndex = cursor.getColumnIndex(DatabaseContract.StudySessions._ID);
        int taskIdIndex = cursor.getColumnIndex(DatabaseContract.StudySessions.COLUMN_TASK_ID);
        int startTimeIndex = cursor.getColumnIndex(DatabaseContract.StudySessions.COLUMN_START_TIME);
        int endTimeIndex = cursor.getColumnIndex(DatabaseContract.StudySessions.COLUMN_END_TIME);
        int durationIndex = cursor.getColumnIndex(DatabaseContract.StudySessions.COLUMN_DURATION);

        if (idIndex >= 0 && !cursor.isNull(idIndex)) session.setId(cursor.getLong(idIndex));
        if (taskIdIndex >= 0 && !cursor.isNull(taskIdIndex)) session.setTaskId(cursor.getLong(taskIdIndex));

        session.setStartTime(startTimeIndex >= 0 && !cursor.isNull(startTimeIndex) ? cursor.getLong(startTimeIndex) : 0L);

        // If end_time is NULL (session still running), keep 0L
        session.setEndTime(endTimeIndex >= 0 && !cursor.isNull(endTimeIndex) ? cursor.getLong(endTimeIndex) : 0L);

        session.setDuration(durationIndex >= 0 && !cursor.isNull(durationIndex) ? cursor.getLong(durationIndex) : 0L);

        return session;
    }
}