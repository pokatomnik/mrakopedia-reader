package com.example.mrakopediareader.api.parser;

import com.example.mrakopediareader.api.dto.Page;

import org.json.JSONException;
import org.json.JSONObject;

public class PageParser extends ParserImpl<Page> {
    private static String KEY_TITLE = "title";
    private static String KEY_URL = "url";

    @Override
    public Page fromJsonObject(JSONObject object) throws JSONException {
        final String title = object.getString(KEY_TITLE);
        final String url = object.getString(KEY_URL);
        return new Page(title, url);
    }
}
