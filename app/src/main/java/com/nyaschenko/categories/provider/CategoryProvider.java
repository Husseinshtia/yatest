package com.nyaschenko.categories.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class CategoryProvider extends ContentProvider {

    private CategoryDatabase openHelper;

    private static UriMatcher uriMatcher = buildMatcher();

    private static final int CATEGORY = 100;
    private static final int CATEGORY_ID = 101;
    private static final int CATEGORY_CHILDREN = 102;

    private static UriMatcher buildMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CategoryContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "category", CATEGORY);
        matcher.addURI(authority, "category/#", CATEGORY_ID);
        matcher.addURI(authority, "category/chidlren/#", CATEGORY_CHILDREN);

        return matcher;
    }

    public CategoryProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        final int rows;

        switch (match) {
            case CATEGORY:
                rows = db.delete(CategoryDatabase.Tables.CATEGORIES, null, null);
                db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{ CategoryDatabase.Tables.CATEGORIES });//new String[]{ "'" + CategoryDatabase.DATABASE_NAME + "'" }
                break;
            default:
                throw new IllegalArgumentException("Illegal delete uri: " + uri);
        }

        return rows;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final long id;

        switch (match) {
            case CATEGORY:
                id = db.insert(CategoryDatabase.Tables.CATEGORIES, null, values);
                return CategoryContract.Category.buildCategoryUri(Long.toString(id));
            default:
                throw new IllegalArgumentException("Illegal insert uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        openHelper = new CategoryDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = openHelper.getReadableDatabase();
        final int match = uriMatcher.match(uri);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (match) {
            case CATEGORY:
                builder.setTables(CategoryDatabase.Tables.CATEGORIES);
                break;
            case CATEGORY_ID:
                builder.setTables(CategoryDatabase.Tables.CATEGORIES);
                builder.appendWhere(CategoryContract.Category._ID + " = " + uri.getLastPathSegment());
                break;
            case CATEGORY_CHILDREN:
                builder.setTables(CategoryDatabase.Tables.CATEGORIES);
                builder.appendWhere(CategoryContract.Category.CATEGORY_PARENT + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("No such uri match: " + match);
        }
        return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }*/
}