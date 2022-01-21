package com.example.mrakopediareader.pageslist

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Page
import com.google.common.net.UrlEscapers
import io.reactivex.rxjava3.core.Observable

class RelatedList : PagesList() {
    private fun setActionBarTitle(actionBar: ActionBar) {
        intent.getStringExtra(resources.getString(R.string.pass_page_title))?.also {
            actionBar.title = "Похожие на $it"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.also(::setActionBarTitle)
    }

    override fun getPages(): Observable<List<Page>> {
        val titleNullable = intent
            .getStringExtra(resources.getString(R.string.pass_page_title))
        return titleNullable?.let { title ->
            Observable
                .just(title)
                .distinctUntilChanged()
                .map { UrlEscapers.urlPathSegmentEscaper().escape(it) }
                .switchMap(api::getRelatedPages)
        } ?: Observable.never()
    }
}