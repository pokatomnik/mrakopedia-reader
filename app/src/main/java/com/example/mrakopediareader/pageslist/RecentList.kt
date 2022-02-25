package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.db.Database
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class RecentList : PagesList() {
    @Inject
    lateinit var database: Database

    override fun getPages(): Observable<List<Page>> = runBlocking {
        val pages = withContext(Dispatchers.IO) {
            database.recentDao().getAll()
                .sortedWith { a, b -> (b.opened - a.opened).toInt() }
                .map { recent ->
                    Page(recent.title, recent.url)
                }
        }

        Observable.just(pages)
    }

    override fun pagesSorted(sortID: PagesSorter.Companion.SortID, pages: List<Page>): List<Page> {
        return pages
    }

    override fun isSortButtonsVisible(): Boolean {
        return false
    }
}