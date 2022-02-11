package com.example.mrakopediareader.api.dto

data class Page(val title: String, val url: String) {
    fun serialize(): String {
        return "$title$JOINER$url"
    }

    companion object {
        const val JOINER = "|"

        fun parse(source: String): Page {
            val (title, url) = source.split(JOINER)
            return Page(title, url)
        }
    }
}