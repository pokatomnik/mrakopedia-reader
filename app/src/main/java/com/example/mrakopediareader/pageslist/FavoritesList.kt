package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.MRReaderApplication
import com.example.mrakopediareader.api.dto.Page
import io.reactivex.rxjava3.core.Observable

class FavoritesList : PagesList() {
    override fun getPages(): Observable<List<Page>> {
        val application = application as MRReaderApplication
        val favoritesStore = FavoritesStore(application.database)
        return favoritesStore.getAll().map {
            it.sortedWith { a, b -> a.title.lowercase().compareTo(b.title.lowercase()) }
        }.toObservable()
    }
}