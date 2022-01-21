package com.example.mrakopediareader.pageslist

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Page
import com.google.common.net.UrlEscapers
import io.reactivex.rxjava3.core.Observable

class PagesByCategory : PagesList() {
    private fun setActionBarTitle(actionBar: ActionBar) {
        intent.getStringExtra(resources.getString(R.string.pass_category_name))
            ?.also { actionBar.title = "Категория: $it" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.also(::setActionBarTitle)
    }

    override fun getPages(): Observable<List<Page>> {
        val categoryNameNullable = intent
            .getStringExtra(resources.getString(R.string.pass_category_name))
        return categoryNameNullable?.let { categoryName ->
            Observable
                .just(categoryName)
                .distinctUntilChanged()
                .map { UrlEscapers.urlPathSegmentEscaper().escape(it) }
                .switchMap(api::getPagesByCategory)
        } ?: Observable.never()
    }
}