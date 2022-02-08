package com.example.mrakopediareader.api.parser

import com.example.mrakopediareader.api.dto.PageMetaInfo
import org.json.JSONObject

internal class PageMetaInfoParser : ParserImpl<Map<String, PageMetaInfo>>() {
    override fun fromJsonObject(jsonObject: JSONObject): Map<String, PageMetaInfo> {
        val pagesIndex = mutableMapOf<String, PageMetaInfo>()
        for (key in jsonObject.keys()) {
            val jsonMetaInfo = jsonObject.getJSONObject(key)
            val pageMetaInfo = PageMetaInfo(jsonMetaInfo.getInt(KEY_READABLE_CHARACTERS))
            pagesIndex[key] = pageMetaInfo
        }
        return pagesIndex
    }

    companion object {
        private const val KEY_READABLE_CHARACTERS = "readableCharacters"
    }
}