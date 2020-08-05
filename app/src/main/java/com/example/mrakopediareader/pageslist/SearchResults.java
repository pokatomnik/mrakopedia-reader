package com.example.mrakopediareader.pageslist;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.dto.Page;
import com.google.common.net.UrlEscapers;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;

public class SearchResults extends PagesList {
    @Override
    protected Observable<ArrayList<Page>> getPages() {
        final String nullableSearchText = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_search_string_intent_key));

        return Observable
                .just(Optional.ofNullable(nullableSearchText))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinctUntilChanged()
                .map((searchText) -> UrlEscapers.urlPathSegmentEscaper().escape(searchText))
                .switchMap((encoded) -> {
                    assert api != null;
                    return api.searchByText(encoded);
                });
    }
}
