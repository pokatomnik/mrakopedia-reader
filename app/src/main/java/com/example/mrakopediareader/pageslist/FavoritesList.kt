package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.api.dto.Page
import io.reactivex.rxjava3.core.Observable

class FavoritesList : PagesList() {
    override fun getPages(): Observable<List<Page>> {
        val favoritesStore = FavoritesStore(baseContext)
        val pagesSorted = favoritesStore.pages.sortedWith { a, b -> a.title.compareTo(b.title) }
        return Observable.just(pagesSorted)
    }
}