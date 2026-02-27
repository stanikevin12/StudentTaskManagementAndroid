package com.example.studenttaskmanagement.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.studenttaskmanagement.database.AppDatabaseHelper;
import com.example.studenttaskmanagement.database.DatabaseContract;
import com.example.studenttaskmanagement.model.Priority;

import java.util.ArrayList;
import java.util.List;

public class PriorityDao {

    private final AppDatabaseHelper databaseHelper;

    public PriorityDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    public List<Priority> getAllPriorities() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<Priority> priorities = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseContract.Priorities.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.Priorities._ID + " ASC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    priorities.add(mapCursorToPriority(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return priorities;
    }

    @Nullable
    public Priority getPriorityById(int id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Priorities.TABLE_NAME,
                null,
                DatabaseContract.Priorities._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                "1"
        );

        if (cursor == null) return null;

        try {
            if (!cursor.moveToFirst()) return null;
            return mapCursorToPriority(cursor);
        } finally {
            cursor.close();
        }
    }

    private Priority mapCursorToPriority(Cursor cursor) {
        Priority priority = new Priority();

        int idIndex = cursor.getColumnIndex(DatabaseContract.Priorities._ID);
        int labelIndex = cursor.getColumnIndex(DatabaseContract.Priorities.COLUMN_LABEL);

        if (idIndex >= 0 && !cursor.isNull(idIndex)) priority.setId(cursor.getLong(idIndex));
        if (labelIndex >= 0 && !cursor.isNull(labelIndex)) priority.setLabel(cursor.getString(labelIndex));

        return priority;
    }
}
