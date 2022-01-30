package com.example.mrakopediareader.api.dto

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "favorites")
data class Page(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "url") val url: String
)