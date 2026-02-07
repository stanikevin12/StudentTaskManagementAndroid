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

    /**
     * Inserts a category row into the database.
     *
     * @param category category data to insert.
     * @return row ID of inserted category, or -1 if insertion failed.
     */
    public long insertCategory(Category category) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (category.getName() != null) {
            values.put(DatabaseContract.Categories.COLUMN_NAME, category.getName());
        } else {
            values.putNull(DatabaseContract.Categories.COLUMN_NAME);
        }

        if (category.getColor() != null) {
            values.put(DatabaseContract.Categories.COLUMN_COLOR, category.getColor());
        } else {
            values.putNull(DatabaseContract.Categories.COLUMN_COLOR);
        }

        return db.insert(DatabaseContract.Categories.TABLE_NAME, null, values);
    }

    /**
     * Returns a category by its ID.
     *
     * @param id category primary key.
     * @return Category when found, otherwise null.
     */
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

    /**
     * Returns all categories ordered by ID ascending.
     *
     * @return list of categories, empty if none exist.
     */
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

    /**
     * Deletes a category by ID.
     *
     * @param id category primary key.
     * @return number of affected rows.
     */
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
            category.setId((int) cursor.getLong(idIndex));
        }

        category.setName(nameIndex >= 0 && !cursor.isNull(nameIndex) ? cursor.getString(nameIndex) : null);
        category.setColor(colorIndex >= 0 && !cursor.isNull(colorIndex) ? cursor.getString(colorIndex) : null);

        return category;
    }
}
