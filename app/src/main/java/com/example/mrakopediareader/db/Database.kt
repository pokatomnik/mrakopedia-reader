package com.example.mrakopediareader.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mrakopediareader.db.dao.favorites.Favorite
import com.example.mrakopediareader.db.dao.favorites.FavoritesDao
import com.example.mrakopediareader.db.dao.recent.Recent
import com.example.mrakopediareader.db.dao.recent.RecentDao
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPosition
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPositionDao

@Database(
    entities = [Favorite::class, ScrollPosition::class, Recent::class],
    version = 1,
//    https://developer.android.com/training/data-storage/room/migrating-db-versions#groovy
//    autoMigrations = [
//        AutoMigration (from = 1, to = 2),
//        AutoMigration (from = 2, to = 3)
//    ],
    exportSchema = true,
)
abstract class Database : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    abstract fun scrollPositionsDao(): ScrollPositionDao

    abstract fun recentDao(): RecentDao
}