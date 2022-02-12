package com.example.mrakopediareader.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mrakopediareader.db.dao.favorites.Favorite
import com.example.mrakopediareader.db.dao.favorites.FavoritesDao
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPosition
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPositionDao

@Database(entities = [Favorite::class, ScrollPosition::class], version = 2, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    abstract fun scrollPositionsDao(): ScrollPositionDao
}