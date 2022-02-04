package com.example.mrakopediareader

import com.example.mrakopediareader.db.dao.Database
import com.example.mrakopediareader.db.dao.favorites.Favorite

class FavoritesStore(private val database: Database) {
    fun remove(title: String) {
        return database.favoritesDao().delete(title)
    }

    fun set(favorite: Favorite) {
        database.favoritesDao().insert(favorite)
    }

    fun getAll(): List<Favorite> {
        return database.favoritesDao().getAll()
    }

    fun has(title: String): Boolean {
        return database.favoritesDao().exists(title)
    }

}