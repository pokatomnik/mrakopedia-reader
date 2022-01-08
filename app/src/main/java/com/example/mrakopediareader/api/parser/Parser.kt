package com.example.mrakopediareader.api.parser

import kotlin.Throws
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONArray
import java.util.ArrayList

interface Parser<T> {
    @Throws(JSONException::class)
    fun fromJsonObject(jsonObject: JSONObject): T

    @Throws(JSONException::class)
    fun fromJsonArray(jsonArray: JSONArray): ArrayList<T>
}