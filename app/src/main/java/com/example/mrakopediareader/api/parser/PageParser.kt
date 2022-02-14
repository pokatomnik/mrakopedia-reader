package com.example.mrakopediareader.api.parser

import com.example.mrakopediareader.api.dto.Page
import org.json.JSONObject

internal class PageParser : ParserImpl<Page>() {
    override fun fromJsonObject(jsonObject: JSONObject): Page {
        val title = jsonObject.getString(KEY_TITLE)
        val url = jsonObject.getString(KEY_URL)
        return Page(title, url)
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_URL = "url"
    }
}