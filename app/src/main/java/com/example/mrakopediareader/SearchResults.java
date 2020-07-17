package com.example.mrakopediareader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.Page;
import com.google.common.net.UrlEscapers;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class SearchResults extends AppCompatActivity {
    private RecyclerView.Adapter mAdapter;

    private API api;

    private ArrayList<Page> searchResults = new ArrayList<>();

    private Subject<LoadingState> loadingSubj$ = PublishSubject.create();

    @Nullable
    private Disposable resultsSub$;

    @Nullable
    private Disposable loadingSub$;

    private void manageVisibility(LoadingState loadingState) {
        final RecyclerView recyclerView = findViewById(R.id.searchResultsView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView noItems = findViewById(R.id.noItems);
        if (loadingState == LoadingState.HAS_ERROR) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.INVISIBLE);

            final Context context = getApplicationContext();
            final String text = getResources().getString(R.string.failed_search_message);
            final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        } else if (loadingState == LoadingState.LOADING) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            noItems.setVisibility(View.INVISIBLE);
        } else if (loadingState == LoadingState.EMPTY) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.VISIBLE);
        } else if (loadingState == LoadingState.HAS_RESULTS) {
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.INVISIBLE);
        }
    }

    private void handleResults(ArrayList<Page> newResults) {
        this.searchResults.clear();
        this.searchResults.addAll(newResults);
        this.mAdapter.notifyDataSetChanged();
    }

    private void handleError(Throwable ignored) {
        this.loadingSubj$.onNext(LoadingState.HAS_ERROR);
    }

    private void handleClick(Page page) {
        final Intent intent = new Intent(getBaseContext(), ViewPage.class);
        intent.putExtra(
                getResources().getString(R.string.pass_page_url),
                this.api.getFullPagePath(page.getUrl())
        );
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        RecyclerView recyclerView = findViewById(R.id.searchResultsView);

        this.loadingSub$ = this.loadingSubj$.subscribe(this::manageVisibility);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new PageResultsAdapter(this.searchResults, this::handleClick);
        recyclerView.setAdapter(mAdapter);

        this.api = new API(getResources(), Volley.newRequestQueue(this));

        final String nullableSearchText = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_search_string_intent_key));

        this.resultsSub$ = Observable
                .just(Optional.ofNullable(nullableSearchText))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinctUntilChanged()
                .doOnNext((ignored) -> SearchResults.this.loadingSubj$.onNext(LoadingState.LOADING))
                .map((searchText) -> UrlEscapers.urlPathSegmentEscaper().escape(searchText))
                .switchMap((encoded) -> SearchResults.this.api.searchByText(encoded))
                .doOnNext((results) -> {
                    if (results.size() == 0) {
                        SearchResults.this.loadingSubj$.onNext(LoadingState.EMPTY);
                    } else {
                        SearchResults.this.loadingSubj$.onNext(LoadingState.HAS_RESULTS);
                    }
                })
                .subscribe(this::handleResults, this::handleError);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.loadingSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.resultsSub$).ifPresent(Disposable::dispose);
    }
}
