package com.example.mrakopediareader.api.parser;

import android.net.Uri;

import com.example.mrakopediareader.api.dto.WebsiteUrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class WebsiteURLParser extends ParserImpl<WebsiteUrl> {
    private static String KEY_TITLE = "title";
    private static String KEY_URL = "url";

    @Override
    public WebsiteUrl fromJsonObject(JSONObject object) throws JSONException {
        final String title = object.getString(KEY_TITLE);
        final Uri uri = Uri.parse(object.getString(KEY_URL));
        return new WebsiteUrl(title, uri);
    }
}
