package com.example.mrakopediareader.api.parser

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface Parser<T> {
    @Throws(JSONException::class)
    fun fromJsonObject(jsonObject: JSONObject): T

    @Throws(JSONException::class)
    fun fromJsonArray(jsonArray: JSONArray): List<T>
}