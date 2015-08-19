package com.nyaschenko.categories.provider;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class CategoryDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "categories.db";

    private static int VERSION = 1;

    interface Tables {
        String CATEGORIES = "categories";
    }

    public CategoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CATEGORIES + " ("
                        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + CategoryContract.CategoryColumns.CATEGORY_ID + " INTEGER, "
                        + CategoryContract.CategoryColumns.CATEGORY_TITLE + " TEXT, "
                        + CategoryContract.CategoryColumns.CATEGORY_PARENT + " INTEGER, "
                        + CategoryContract.CategoryColumns.CATEGORY_HAS_SUBCATEGORIES + " INTEGER NOT NULL);"
        );

        /*db.execSQL(createEntry(123, "Products", null, 0));
        db.execSQL(createEntry(456, "Games", null, 1));
        db.execSQL(createEntry(11, "Bubbles", 2L, 0));
        db.execSQL(createEntry(22, "Tetris", 2L, 0));*/
    }

    private static String createEntry(long id, String title, Long parentId, int hasChildren) {
        return "INSERT INTO " + Tables.CATEGORIES + "("
                + CategoryContract.CategoryColumns.CATEGORY_ID + ", "
                + CategoryContract.CategoryColumns.CATEGORY_TITLE + ", "
                + CategoryContract.CategoryColumns.CATEGORY_PARENT + ", "
                + CategoryContract.CategoryColumns.CATEGORY_HAS_SUBCATEGORIES +
                ") VALUES (" +
                id + ", " +
                DatabaseUtils.sqlEscapeString(title) + ", " +
                parentId + ", " +
                hasChildren + ");";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}