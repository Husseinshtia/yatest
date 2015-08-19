package com.nyaschenko.categories;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyaschenko.categories.provider.CategoryContract;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class LoadService extends IntentService {
    private static final String ACTION_LOAD_CATEGORIES = "com.nyaschenko.catlist.action.LOAD_CATEGORIES";

    private static final String CATEGORIES_URL = "https://money.yandex.ru/api/categories-list";

    public static void startActionLoadCategories(Context context) {
        Intent intent = new Intent(context, LoadService.class);
        intent.setAction(ACTION_LOAD_CATEGORIES);
        context.startService(intent);
    }

    public LoadService() {
        super("LoadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_LOAD_CATEGORIES:
                    handleActionLoadCategories();
                    break;
            }
        }
    }

    private void handleActionLoadCategories() {
        try {
            loadCategories();
        } catch (IOException e) {
            //TODO tell that smth went wrong
        }
    }

    private void loadCategories() throws IOException {
        final URL url = new URL(CATEGORIES_URL);
        if (!isOnline()) {
            return;
        }
        String json = new Scanner(url.openStream()).useDelimiter("\\A").next();

        getContentResolver().delete(CategoryContract.Category.CONTENT_URI, null, null);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        addChildren(node, -1);
    }

    private void addChildren(JsonNode node, long parentId) {
        for (JsonNode category : node) {
            ContentValues cv = new ContentValues();
            if (parentId != -1) {
                cv.put(CategoryContract.CategoryColumns.CATEGORY_PARENT, parentId);
            }
            if (category.has("id")) {
                cv.put(CategoryContract.CategoryColumns.CATEGORY_ID, category.get("id").asLong());
            }
            if (category.has("title")) {
                cv.put(CategoryContract.CategoryColumns.CATEGORY_TITLE, category.get("title").textValue());
            }
            final int hasSubcategories = category.has("subs") ? 1 : 0;
            cv.put(CategoryContract.CategoryColumns.CATEGORY_HAS_SUBCATEGORIES, hasSubcategories);

            Uri uri = getContentResolver().insert(CategoryContract.Category.CONTENT_URI, cv);
            long id = Long.parseLong(uri.getLastPathSegment());

            if (hasSubcategories == 1) {
                addChildren(category.get("subs"), id);
            }
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}