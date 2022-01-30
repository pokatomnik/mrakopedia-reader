package com.example.mrakopediareader.db.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mrakopediareader.api.dto.Page

@Database(entities = [Page::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}