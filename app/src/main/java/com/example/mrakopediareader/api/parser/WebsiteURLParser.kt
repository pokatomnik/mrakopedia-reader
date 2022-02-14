package com.example.mrakopediareader.api.parser

import android.net.Uri
import com.example.mrakopediareader.api.dto.WebsiteUrl
import org.json.JSONObject

internal class WebsiteURLParser : ParserImpl<WebsiteUrl>() {
    override fun fromJsonObject(jsonObject: JSONObject): WebsiteUrl {
        val title = jsonObject.getString(KEY_TITLE)
        val uri = Uri.parse(jsonObject.getString(KEY_URL))
        return WebsiteUrl(title, uri)
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_URL = "url"
    }
}