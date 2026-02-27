package com.example.studenttaskmanagement.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

/**
 * SQLiteOpenHelper implementation for the Student Task Management System.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "student_task_management.db";
    private static final int DATABASE_VERSION = 4;

    public AppDatabaseHelper(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUsersTable());
        db.execSQL(createCategoriesTable());
        db.execSQL(createPrioritiesTable());
        db.execSQL(createTasksTable());
        db.execSQL(createStudySessionsTable());
        db.execSQL(createAttachmentsTable());
        db.execSQL(createNotificationsTable());

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since we're changing schema, easiest is recreate for now:
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Notifications.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Attachments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.StudySessions.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Tasks.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Priorities.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Categories.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Users.TABLE_NAME);

        onCreate(db);
    }

    private String createUsersTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.Users.TABLE_NAME + " ("
                + DatabaseContract.Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Users.COLUMN_NAME + " TEXT, "
                + DatabaseContract.Users.COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
                + DatabaseContract.Users.COLUMN_PASSWORD_HASH + " TEXT NOT NULL, "
                + DatabaseContract.Users.COLUMN_CREATED_AT + " INTEGER"
                + ")";
    }

    private String createCategoriesTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.Categories.TABLE_NAME + " ("
                + DatabaseContract.Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Categories.COLUMN_NAME + " TEXT NOT NULL, "
                + DatabaseContract.Categories.COLUMN_COLOR + " TEXT"
                + ")";
    }

    private String createPrioritiesTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.Priorities.TABLE_NAME + " ("
                + DatabaseContract.Priorities._ID + " INTEGER PRIMARY KEY, "
                + DatabaseContract.Priorities.COLUMN_LABEL + " TEXT"
                + ")";
    }

    private String createTasksTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.Tasks.TABLE_NAME + " ("
                + DatabaseContract.Tasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Tasks.COLUMN_TITLE + " TEXT NOT NULL, "
                + DatabaseContract.Tasks.COLUMN_DESCRIPTION + " TEXT, "
                + DatabaseContract.Tasks.COLUMN_DEADLINE + " TEXT, "
                + DatabaseContract.Tasks.COLUMN_STATUS + " INTEGER DEFAULT 0, "
                + DatabaseContract.Tasks.COLUMN_CATEGORY_ID + " INTEGER, "
                + DatabaseContract.Tasks.COLUMN_PRIORITY_ID + " INTEGER, "
                + DatabaseContract.Tasks.COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + DatabaseContract.Tasks.COLUMN_CATEGORY_ID + ") REFERENCES "
                + DatabaseContract.Categories.TABLE_NAME + "(" + DatabaseContract.Categories._ID + "), "
                + "FOREIGN KEY(" + DatabaseContract.Tasks.COLUMN_PRIORITY_ID + ") REFERENCES "
                + DatabaseContract.Priorities.TABLE_NAME + "(" + DatabaseContract.Priorities._ID + "), "
                + "FOREIGN KEY(" + DatabaseContract.Tasks.COLUMN_USER_ID + ") REFERENCES "
                + DatabaseContract.Users.TABLE_NAME + "(" + DatabaseContract.Users._ID + ")"
                + ")";
    }

    private String createStudySessionsTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.StudySessions.TABLE_NAME + " ("
                + DatabaseContract.StudySessions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.StudySessions.COLUMN_TASK_ID + " INTEGER, "
                + DatabaseContract.StudySessions.COLUMN_START_TIME + " INTEGER, " // <-- was TEXT
                + DatabaseContract.StudySessions.COLUMN_END_TIME + " INTEGER, "   // <-- was TEXT
                + DatabaseContract.StudySessions.COLUMN_DURATION + " INTEGER, "
                + "FOREIGN KEY(" + DatabaseContract.StudySessions.COLUMN_TASK_ID + ") REFERENCES "
                + DatabaseContract.Tasks.TABLE_NAME + "(" + DatabaseContract.Tasks._ID + ") ON DELETE CASCADE"
                + ")";
    }

    private String createAttachmentsTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.Attachments.TABLE_NAME + " ("
                + DatabaseContract.Attachments._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Attachments.COLUMN_TASK_ID + " INTEGER, "
                + DatabaseContract.Attachments.COLUMN_FILE_PATH + " TEXT, "
                + DatabaseContract.Attachments.COLUMN_TYPE + " TEXT, "
                + "FOREIGN KEY(" + DatabaseContract.Attachments.COLUMN_TASK_ID + ") REFERENCES "
                + DatabaseContract.Tasks.TABLE_NAME + "(" + DatabaseContract.Tasks._ID + ") ON DELETE CASCADE"
                + ")";
    }

    private String createNotificationsTable() {
        return "CREATE TABLE IF NOT EXISTS " + DatabaseContract.Notifications.TABLE_NAME + " ("
                + DatabaseContract.Notifications._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Notifications.COLUMN_TASK_ID + " INTEGER NOT NULL, "
                + DatabaseContract.Notifications.COLUMN_NOTIFY_TIME + " INTEGER NOT NULL, "
                + DatabaseContract.Notifications.COLUMN_IS_SENT + " INTEGER NOT NULL DEFAULT 0, "
                + "UNIQUE(" + DatabaseContract.Notifications.COLUMN_TASK_ID + "), "
                + "FOREIGN KEY(" + DatabaseContract.Notifications.COLUMN_TASK_ID + ") REFERENCES "
                + DatabaseContract.Tasks.TABLE_NAME + "(" + DatabaseContract.Tasks._ID + ") ON DELETE CASCADE"
                + ")";
    }

    private void seedInitialData(SQLiteDatabase db) {
        seedDefaultUser(db);
        seedPriorities(db);
        seedCategories(db);
    }

    private void seedDefaultUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Users._ID, 1);
        values.put(DatabaseContract.Users.COLUMN_NAME, "Default Student");
        values.put(DatabaseContract.Users.COLUMN_EMAIL, "student@example.com");
        values.put(DatabaseContract.Users.COLUMN_PASSWORD_HASH, "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
        values.put(DatabaseContract.Users.COLUMN_CREATED_AT, System.currentTimeMillis());
        db.insertWithOnConflict(DatabaseContract.Users.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void seedPriorities(SQLiteDatabase db) {
        insertPriority(db, 1, "Low");
        insertPriority(db, 2, "Medium");
        insertPriority(db, 3, "High");
    }

    private void insertPriority(SQLiteDatabase db, int id, String label) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Priorities._ID, id);
        values.put(DatabaseContract.Priorities.COLUMN_LABEL, label);
        db.insertWithOnConflict(DatabaseContract.Priorities.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void seedCategories(SQLiteDatabase db) {
        insertCategory(db, "Homework", "#2196F3");
        insertCategory(db, "Exam", "#F44336");
        insertCategory(db, "Project", "#4CAF50");
    }

    private void insertCategory(SQLiteDatabase db, String name, String color) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Categories.COLUMN_NAME, name);
        values.put(DatabaseContract.Categories.COLUMN_COLOR, color);
        db.insertWithOnConflict(DatabaseContract.Categories.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}