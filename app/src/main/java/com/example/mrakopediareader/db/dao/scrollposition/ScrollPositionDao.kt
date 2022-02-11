package com.example.mrakopediareader.db.dao.scrollposition

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScrollPositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setPosition(position: ScrollPosition)

    @Query("SELECT * FROM scrollpositions WHERE title = :title")
    fun getPosition(title: String): ScrollPosition?
}