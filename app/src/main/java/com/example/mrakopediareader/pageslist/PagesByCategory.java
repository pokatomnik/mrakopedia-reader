package com.example.mrakopediareader.pageslist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.Page;
import com.google.common.net.UrlEscapers;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;

public class PagesByCategory extends PagesList {
    private void setActionBarTitle(ActionBar actionBar) {
        final Intent intent = getIntent();
        final String nullableCategoryName = intent.getStringExtra(
                getResources().getString(R.string.pass_category_name)
        );
        Optional.ofNullable(nullableCategoryName).ifPresent((categoryName) -> {
            final String title = String.format("Категория: %s", categoryName);
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
