package com.example.mrakopediareader.db.dao.scrollposition

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scrollpositions")
data class ScrollPosition(
    @PrimaryKey @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "position") val position: Int
)