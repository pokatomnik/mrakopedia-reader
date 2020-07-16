package com.example.mrakopediareader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
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

    private Subject<Boolean> loadingSubj$ = PublishSubject.create();

    @Nullable
    private Disposable resultsSub$;

    @Nullable
    private Disposable loadingSub$;

    private void handleLoading(boolean isLoading) {
        final RecyclerView recyclerView = findViewById(R.id.searchResultsView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        if (isLoading) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void handleResults(ArrayList<Page> newResults) {
        this.searchResults.clear();
        this.searchResults.addAll(newResults);
        this.mAdapter.notifyDataSetChanged();
    }

    private void handleError(Throwable ignored) {
        final Context context = getApplicationContext();
        String text = getResources().getString(R.string.failed_search_message);
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
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

        this.loadingSub$ = this.loadingSubj$.subscribe(this::handleLoading);

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
                .doOnEach((ignored) -> SearchResults.this.loadingSubj$.onNext(true))
                .map((searchText) -> UrlEscapers.urlPathSegmentEscaper().escape(searchText))
                .switchMap((encoded) -> SearchResults.this.api.searchByText(encoded))
                .doFinally(() -> SearchResults.this.loadingSubj$.onNext(false))
                .subscribe(this::handleResults, this::handleError);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.loadingSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.resultsSub$).ifPresent(Disposable::dispose);
    }
}
