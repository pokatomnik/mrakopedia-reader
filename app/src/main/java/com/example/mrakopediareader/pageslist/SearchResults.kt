package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Page
import com.google.common.net.UrlEscapers
import io.reactivex.rxjava3.core.Observable

class SearchResults : PagesList() {
    override fun getPages(): Observable<List<Page>> {
        val nullableSearchText = intent
            .getStringExtra(resources.getString(R.string.pass_search_string_intent_key))
        return nullableSearchText?.let { searchText ->
            Observable
                .just(searchText)
                .map { UrlEscapers.urlPathSegmentEscaper().escape(it) }
                .switchMap(api::searchByText)
        } ?: Observable.never();
    }
}