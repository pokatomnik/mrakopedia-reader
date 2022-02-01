package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.MRReaderApplication
import com.example.mrakopediareader.api.dto.Page
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavoritesList : PagesList() {
    override fun getPages(): Observable<List<Page>> {
        val application = application as MRReaderApplication
        val favoritesStore = FavoritesStore(application.database)
        val publisher = PublishSubject.create<List<Page>>()

        GlobalScope.launch {
            val pages = favoritesStore.getAll()
                .sortedWith { a, b ->
                    a.title.lowercase().compareTo(b.title.lowercase())
                }
                .map { favorite ->
                    Page(favorite.title, favorite.url)
                }
            publisher.onNext(pages)
        }

        return publisher
    }
}