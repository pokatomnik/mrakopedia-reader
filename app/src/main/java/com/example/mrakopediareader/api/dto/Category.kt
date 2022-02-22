package com.example.mrakopediareader.api.dto

data class Category(val title: String, val url: String) {
    fun serialize(): String {
        return "$title${JOINER}$url"
    }

    companion object {
        private const val JOINER = "|"

        fun parse(source: String): Category {
            val (title, url) = source.split(JOINER)
            return Category(title, url)
        }
    }
}