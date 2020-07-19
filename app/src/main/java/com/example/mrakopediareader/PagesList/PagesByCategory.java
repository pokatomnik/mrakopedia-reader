package com.example.mrakopediareader.PagesList;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.Page;
import com.google.common.net.UrlEscapers;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;

public class PagesByCategory extends PagesList {
    @Override
    protected Observable<ArrayList<Page>> getPages() {
        final String categoryNameNullable = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_category_name));
        return Observable.just(Optional.ofNullable(categoryNameNullable))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinctUntilChanged()
                .map((categoryName) -> UrlEscapers.urlPathSegmentEscaper().escape(categoryName))
                .switchMap((encoded) -> {
                    assert api != null;
                    return api.getPagesByCategory(encoded);
                });

    }
}
