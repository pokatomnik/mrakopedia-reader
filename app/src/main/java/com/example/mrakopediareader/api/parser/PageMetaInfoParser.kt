package com.example.mrakopediareader.api.parser

import com.example.mrakopediareader.api.dto.PageMetaInfo
import org.json.JSONException
import org.json.JSONObject

internal class PageMetaInfoParser : ParserImpl<Map<String, PageMetaInfo>>() {
    private fun tryInt(key: String, jsonObject: JSONObject): Int? = try {
        jsonObject.getInt(key)
    } catch (error: JSONException) {
        null
    }

    override fun fromJsonObject(jsonObject: JSONObject): Map<String, PageMetaInfo> {
        val pagesIndex = mutableMapOf<String, PageMetaInfo>()
        for (key in jsonObject.keys()) {
            val jsonMetaInfo = jsonObject.getJSONObject(key)
            val readableCharacters = tryInt(KEY_READABLE_CHARACTERS, jsonMetaInfo)
            val rating = tryInt(KEY_RATING, jsonMetaInfo)
            val voted = tryInt(KEY_VOTED, jsonMetaInfo)
            val pageMetaInfo = PageMetaInfo(
                readableCharacters = readableCharacters,
                rating = rating,
                voted = voted
            )
            pagesIndex[key] = pageMetaInfo
        }
        return pagesIndex
    }

    companion object {
        private const val KEY_READABLE_CHARACTERS = "readableCharacters"
        private const val KEY_RATING = "rating"
        private const val KEY_VOTED = "voted"
    }
}