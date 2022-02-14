package com.example.mrakopediareader.api.parser

import com.example.mrakopediareader.api.dto.Category
import org.json.JSONObject

internal class CategoryParser : ParserImpl<Category>() {
    override fun fromJsonObject(jsonObject: JSONObject): Category {
        val title = jsonObject.getString(KEY_TITLE)
        val url = jsonObject.getString(KEY_URL)
        return Category(title, url)
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_URL = "url"
    }
}