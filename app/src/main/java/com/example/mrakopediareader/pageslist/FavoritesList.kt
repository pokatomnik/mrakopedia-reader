package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.MRReaderApplication
import com.example.mrakopediareader.api.dto.Page
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class FavoritesList : PagesList() {
    override fun getPages(): Observable<List<Page>> = runBlocking {
        val application = application as MRReaderApplication
        val favoritesStore = FavoritesStore(application.database)

        val pages = withContext(Dispatchers.Default) {
            favoritesStore.getAll()
                .sortedWith { a, b ->
                    a.title.lowercase().compareTo(b.title.lowercase())
                }
                .map { favorite ->
                    Page(favorite.title, favorite.url)
                }
        }
        Observable.just(pages)
    }
}