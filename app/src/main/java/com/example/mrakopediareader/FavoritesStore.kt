package com.example.mrakopediareader

import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.db.dao.Database
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class FavoritesStore(private val database: Database) {
    fun get(title: String): Single<String> {
        return database.favoritesDao().getByTitle(title).map { it.url }
    }

    fun remove(title: String): Completable{
        return database.favoritesDao().delete(title)
    }

    fun set(page: Page): Completable {
        return database.favoritesDao().insert(page)
    }

    fun getAll(): Single<List<Page>> {
        return database.favoritesDao().getAll()
    }

    fun has(title: String): Single<Boolean> {
        return database.favoritesDao().exists(title)
    }

}