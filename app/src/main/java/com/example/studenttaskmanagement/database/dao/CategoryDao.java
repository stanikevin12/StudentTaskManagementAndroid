package com.example.studenttaskmanagement.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studenttaskmanagement.database.AppDatabaseHelper;
import com.example.studenttaskmanagement.database.DatabaseContract;
import com.example.studenttaskmanagement.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CRUD operations on the categories table.
 */
public class CategoryDao {

    private final AppDatabaseHelper databaseHelper;

    public CategoryDao(Context context) {
        this.databaseHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    public long insertCategory(Category category) {
        if (category == null) return -1;

        String name = category.getName();
        if (name == null || name.trim().isEmpty()) {
            return -1; // <-- prevent NOT NULL constraint crash
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Categories.COLUMN_NAME, name.trim());

        if (category.getColor() != null) {
            values.put(DatabaseContract.Categories.COLUMN_COLOR, category.getColor());
        } else {
            values.putNull(DatabaseContract.Categories.COLUMN_COLOR);
        }

        return db.insert(DatabaseContract.Categories.TABLE_NAME, null, values);
    }

    public Category getCategoryById(long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE_NAME,
                null,
                DatabaseContract.Categories._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Category category = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    category = mapCursorToCategory(cursor);
                }
            } finally {
                cursor.close();
            }
        }

        return category;
    }

    public List<Category> getAllCategories() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<Category> categories = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.Categories._ID + " ASC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    categories.add(mapCursorToCategory(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return categories;
    }

    public int deleteCategory(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.Categories.TABLE_NAME,
                DatabaseContract.Categories._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private Category mapCursorToCategory(Cursor cursor) {
        Category category = new Category();

        int idIndex = cursor.getColumnIndex(DatabaseContract.Categories._ID);
        int nameIndex = cursor.getColumnIndex(DatabaseContract.Categories.COLUMN_NAME);
        int colorIndex = cursor.getColumnIndex(DatabaseContract.Categories.COLUMN_COLOR);

        if (idIndex >= 0 && !cursor.isNull(idIndex)) {
            category.setId(cursor.getLong(idIndex));
        }

        category.setName(nameIndex >= 0 && !cursor.isNull(nameIndex) ? cursor.getString(nameIndex) : null);
        category.setColor(colorIndex >= 0 && !cursor.isNull(colorIndex) ? cursor.getString(colorIndex) : null);

        return category;
    }
}