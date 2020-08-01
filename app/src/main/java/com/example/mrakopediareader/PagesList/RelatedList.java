package com.example.mrakopediareader.PagesList;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.Page;
import com.google.common.net.UrlEscapers;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;

public class RelatedList extends PagesList {
    private void setActionBarTitle(ActionBar actionBar) {
        final Intent intent = getIntent();
        final String nullablePageTitle = intent.getStringExtra(
                getResources().getString(R.string.pass_page_title)
        );

        Optional.ofNullable(nullablePageTitle).ifPresent((pageTitle) -> {
            final String title = String.format("Похожие на \"%s\"", pageTitle);
            actionBar.setTitle(title);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Optional.ofNullable(getSupportActionBar()).ifPresent(this::setActionBarTitle);
    }

    @Override
    protected Observable<ArrayList<Page>> getPages() {
        final String titleNullable = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_page_title));
        return Observable.just(Optional.ofNullable(titleNullable))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinctUntilChanged()
                .map((categoryName) -> UrlEscapers.urlPathSegmentEscaper().escape(categoryName))
                .switchMap((encoded) -> {
                    assert api != null;
                    return api.getRelatedPages(encoded);
                });

    }
}
