package com.example.mrakopediareader.db.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mrakopediareader.db.dao.favorites.Favorite
import com.example.mrakopediareader.db.dao.favorites.FavoritesDao

@Database(entities = [Favorite::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}