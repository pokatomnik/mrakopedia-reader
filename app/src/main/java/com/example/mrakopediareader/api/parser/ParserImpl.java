package com.example.mrakopediareader.api.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

abstract class ParserImpl<T> implements Parser<T> {

    @Override
    public ArrayList<T> fromJsonArray(JSONArray array) throws JSONException {
        final int length = array.length();
        final ArrayList<T> results = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            results.add(fromJsonObject(array.getJSONObject(i)));
        }
        return results;
    }
}
