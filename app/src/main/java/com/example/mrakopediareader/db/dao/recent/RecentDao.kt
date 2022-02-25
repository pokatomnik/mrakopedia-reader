package com.example.mrakopediareader.db.dao.recent

import androidx.room.*

@Dao
abstract class RecentDao {
    @Query("SELECT * FROM recent")
    abstract fun getAll(): List<Recent>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(recent: Recent): Long

    @Query("UPDATE recent SET opened = :opened WHERE title = :title")
    abstract fun update(title: String, opened: Long)

    @Transaction
    open fun upsert(recent: Recent) {
        val result = insert(recent)
        if (result == -1L) {
            update(recent.title, recent.opened)
        }
    }
}