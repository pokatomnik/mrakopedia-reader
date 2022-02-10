package com.example.mrakopediareader.csvexport

class CSVColumn<T>(
    val title: String,
    private val serializeItem: (item: T) -> String
) {
    fun getContents(item: T): String {
        return serializeItem(item)
    }
}