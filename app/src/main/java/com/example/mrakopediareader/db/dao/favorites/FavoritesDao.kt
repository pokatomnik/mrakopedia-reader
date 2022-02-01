package com.example.mrakopediareader.db.dao.favorites

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites")
    fun getAll(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg pages: Favorite)

    @Query("DELETE FROM favorites WHERE title = :title")
    fun delete(title: String)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE title = :title)")
    fun exists(title: String): Boolean
}