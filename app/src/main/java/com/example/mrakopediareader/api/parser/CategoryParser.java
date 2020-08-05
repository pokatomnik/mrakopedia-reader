package com.example.mrakopediareader.api.parser;

import com.example.mrakopediareader.api.dto.Category;

import org.json.JSONException;
import org.json.JSONObject;

public class CategoryParser extends ParserImpl<Category> {
    private static String KEY_TITLE = "title";
    private static String KEY_URL = "url";

    @Override
    public Category fromJsonObject(JSONObject object) throws JSONException {
        final String title = object.getString(KEY_TITLE);
        final String url = object.getString(KEY_URL);
        return new Category(title, url);
    }
}
