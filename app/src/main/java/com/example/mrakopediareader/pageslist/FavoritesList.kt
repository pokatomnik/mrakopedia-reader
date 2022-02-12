package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.db.Database
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesList : PagesList() {
    @Inject
    lateinit var database: Database

    override fun getPages(): Observable<List<Page>> = runBlocking {
        val favoritesStore = FavoritesStore(database)

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