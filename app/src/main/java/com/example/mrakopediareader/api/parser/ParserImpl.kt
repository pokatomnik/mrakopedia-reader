package com.example.mrakopediareader.api.parser

import kotlin.Throws
import org.json.JSONException
import org.json.JSONArray
import java.util.ArrayList

internal abstract class ParserImpl<T> : Parser<T> {
    @Throws(JSONException::class)
    override fun fromJsonArray(array: JSONArray): ArrayList<T> {
        val length = array.length()
        val results = ArrayList<T>(length)
        for (i in 0 until length) {
            results.add(fromJsonObject(array.getJSONObject(i)))
        }
        return results
    }
}