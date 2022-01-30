package com.example.mrakopediareader.db.dao

import androidx.room.*
import com.example.mrakopediareader.api.dto.Page
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites")
    fun getAll(): Single<List<Page>>

    @Query("SELECT * FROM favorites WHERE title = :title")
    fun getByTitle(title: String): Single<Page>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg pages: Page): Completable

    @Query("DELETE FROM favorites WHERE title = :title")
    fun delete(title: String): Completable

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE title = :title)")
    fun exists(title: String): Single<Boolean>
}