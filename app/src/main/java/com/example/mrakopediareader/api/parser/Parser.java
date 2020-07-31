package com.example.mrakopediareader.api.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public interface Parser<T> {
    T fromJsonObject(JSONObject object) throws JSONException;

    ArrayList<T> fromJsonArray(JSONArray array) throws JSONException;
}
