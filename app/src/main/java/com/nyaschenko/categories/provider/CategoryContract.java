package com.nyaschenko.categories.provider;


import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by nafanya on 13.08.15.
 */
public class CategoryContract {

    public interface CategoryColumns {
        String CATEGORY_TITLE = "category_title";
        String CATEGORY_ID = "category_id";
        String CATEGORY_PARENT = "category_parent";
    }

    public static final String CONTENT_AUTHORITY = "com.nyaschenko.categories";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_CATEGORY = "category";

    public static class Category implements CategoryColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String[] ALL_COLUMNS = {
                BaseColumns._ID,
                CategoryColumns.CATEGORY_TITLE,
                CategoryColumns.CATEGORY_ID,
                CategoryColumns.CATEGORY_PARENT,
        };

        public static Uri buildCategoryUri(String categoryId) {
            return CONTENT_URI.buildUpon().appendPath(categoryId).build();
        }
    }
}