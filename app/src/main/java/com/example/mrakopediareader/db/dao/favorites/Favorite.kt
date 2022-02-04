package com.example.mrakopediareader.db.dao.favorites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "url") val url: String
) {
    constructor(title: String, url: String) : this(null, title, url)
}