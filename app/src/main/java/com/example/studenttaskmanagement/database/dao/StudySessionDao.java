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
 */
public class StudySessionDao {

    private final AppDatabaseHelper databaseHelper;

    public StudySessionDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * Starts a study session for a task by inserting start time.
     * End time and duration are initially null/zero until the session is closed.
     *
     * @param taskId task ID associated with the session.
     * @param startTime session start time in epoch milliseconds.
     * @return row ID of the inserted session, or -1 if insertion failed.
     */
    public long startSession(long taskId, long startTime) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.StudySessions.COLUMN_TASK_ID, taskId);
        values.put(DatabaseContract.StudySessions.COLUMN_START_TIME, String.valueOf(startTime));
        values.putNull(DatabaseContract.StudySessions.COLUMN_END_TIME);
        values.put(DatabaseContract.StudySessions.COLUMN_DURATION, 0L);

        return db.insert(DatabaseContract.StudySessions.TABLE_NAME, null, values);
    }

    /**
     * Ends an existing study session by writing end time and calculated duration.
     * Duration is computed as (endTime - startTime) and bounded to be non-negative.
     *
     * @param sessionId study session ID.
     * @param endTime session end time in epoch milliseconds.
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
        values.put(DatabaseContract.StudySessions.COLUMN_END_TIME, String.valueOf(endTime));
        values.put(DatabaseContract.StudySessions.COLUMN_DURATION, duration);

        return db.update(
                DatabaseContract.StudySessions.TABLE_NAME,
                values,
                DatabaseContract.StudySessions._ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    /**
     * Returns all study sessions for a given task.
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

        if (cursor == null) {
            return null;
        }

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }

            int startTimeIndex = cursor.getColumnIndex(DatabaseContract.StudySessions.COLUMN_START_TIME);
            if (startTimeIndex < 0 || cursor.isNull(startTimeIndex)) {
                return null;
            }

            String startTimeValue = cursor.getString(startTimeIndex);
            if (startTimeValue == null || startTimeValue.trim().isEmpty()) {
                return null;
            }

            try {
                return Long.parseLong(startTimeValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
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

        if (idIndex >= 0 && !cursor.isNull(idIndex)) {
            session.setId((int) cursor.getLong(idIndex));
        }

        if (taskIdIndex >= 0 && !cursor.isNull(taskIdIndex)) {
            session.setTaskId((int) cursor.getLong(taskIdIndex));
        }

        session.setStartTime(startTimeIndex >= 0 && !cursor.isNull(startTimeIndex)
                ? cursor.getString(startTimeIndex)
                : null);

        session.setEndTime(endTimeIndex >= 0 && !cursor.isNull(endTimeIndex)
                ? cursor.getString(endTimeIndex)
                : null);

        if (durationIndex >= 0 && !cursor.isNull(durationIndex)) {
            session.setDuration((int) cursor.getLong(durationIndex));
        } else {
            session.setDuration(0);
        }

        return session;
    }
}
