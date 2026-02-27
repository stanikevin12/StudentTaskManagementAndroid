package com.example.studenttaskmanagement.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.studenttaskmanagement.database.AppDatabaseHelper;
import com.example.studenttaskmanagement.database.DatabaseContract;
import com.example.studenttaskmanagement.model.User;

public class UserDao {

    private final AppDatabaseHelper databaseHelper;

    public UserDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    public long insertUser(User user) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Users.COLUMN_NAME, user.getName());
        values.put(DatabaseContract.Users.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseContract.Users.COLUMN_PASSWORD_HASH, user.getPasswordHash());
        values.put(DatabaseContract.Users.COLUMN_CREATED_AT, user.getCreatedAt());
        return db.insert(DatabaseContract.Users.TABLE_NAME, null, values);
    }

    @Nullable
    public User getUserByEmail(String email) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.Users.TABLE_NAME,
                null,
                DatabaseContract.Users.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null,
                "1"
        );

        if (cursor == null) return null;

        try {
            if (!cursor.moveToFirst()) return null;
            return mapCursorToUser(cursor);
        } finally {
            cursor.close();
        }
    }

    private User mapCursorToUser(Cursor cursor) {
        User user = new User();

        int idIndex = cursor.getColumnIndex(DatabaseContract.Users._ID);
        int nameIndex = cursor.getColumnIndex(DatabaseContract.Users.COLUMN_NAME);
        int emailIndex = cursor.getColumnIndex(DatabaseContract.Users.COLUMN_EMAIL);
        int passwordIndex = cursor.getColumnIndex(DatabaseContract.Users.COLUMN_PASSWORD_HASH);
        int createdAtIndex = cursor.getColumnIndex(DatabaseContract.Users.COLUMN_CREATED_AT);

        if (idIndex >= 0 && !cursor.isNull(idIndex)) user.setId(cursor.getLong(idIndex));
        if (nameIndex >= 0 && !cursor.isNull(nameIndex)) user.setName(cursor.getString(nameIndex));
        if (emailIndex >= 0 && !cursor.isNull(emailIndex)) user.setEmail(cursor.getString(emailIndex));
        if (passwordIndex >= 0 && !cursor.isNull(passwordIndex)) user.setPasswordHash(cursor.getString(passwordIndex));
        if (createdAtIndex >= 0 && !cursor.isNull(createdAtIndex)) user.setCreatedAt(cursor.getLong(createdAtIndex));

        return user;
    }
}
