package com.example.mrakopediareader.pageslist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.LoadingState;
import com.example.mrakopediareader.R;
import com.example.mrakopediareader.ViewPage;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.dto.Page;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

abstract class PagesList extends AppCompatActivity {
    private RecyclerView.Adapter<PageResultsAdapter.ViewHolder> mAdapter;

    @Nullable
    protected API api;

    private ArrayList<Page> pagesList = new ArrayList<>();

    private Subject<LoadingState> loadingSubj$ = PublishSubject.create();

    @Nullable
    private Disposable resultsSub$;

    @Nullable
    private Disposable loadingSub$;

    protected abstract Observable<ArrayList<Page>> getPages();

    private void manageVisibility(LoadingState loadingState) {
        final RecyclerView recyclerView = findViewById(R.id.pagesListView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView noItems = findViewById(R.id.noItems);
        if (loadingState == LoadingState.HAS_ERROR) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.INVISIBLE);

            final Context context = getApplicationContext();
            final String text = getResources().getString(R.string.notification_failed_get_pages_message);
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
        this.pagesList.clear();
        this.pagesList.addAll(newResults);
        this.mAdapter.notifyDataSetChanged();
    }

    private void handleError(Throwable ignored) {
        this.loadingSubj$.onNext(LoadingState.HAS_ERROR);
    }

    private void handleClick(Page page) {
        Optional.ofNullable(this.api).ifPresent((api) -> {
            final Intent intent = new Intent(getBaseContext(), ViewPage.class);
            final Resources resources = getResources();
            intent.putExtra(
                    resources.getString(R.string.pass_page_url),
                    api.getFullPagePath(page.getUrl())
            );
            intent.putExtra(
                    resources.getString(R.string.pass_page_title),
                    page.getTitle()
            );
            intent.putExtra(
                    resources.getString(R.string.pass_page_path),
                    page.getUrl()
            );
            startActivity(intent);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pages_list);

        Optional.ofNullable(getSupportActionBar()).ifPresent(((actionBar) -> {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }));

        RecyclerView recyclerView = findViewById(R.id.pagesListView);

        this.loadingSub$ = this.loadingSubj$.subscribe(this::manageVisibility);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new PageResultsAdapter(this.pagesList, this::handleClick);
        recyclerView.setAdapter(mAdapter);

        this.api = new API(getResources(), Volley.newRequestQueue(this));

        loadingSubj$.onNext(LoadingState.LOADING);
        this.resultsSub$ = this.getPages()
                .doOnNext((results) -> {
                    if (results.size() == 0) {
                        loadingSubj$.onNext(LoadingState.EMPTY);
                    } else {
                        loadingSubj$.onNext(LoadingState.HAS_RESULTS);
                    }
                })
                .subscribe(this::handleResults, this::handleError);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            this.onBackPressed();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.loadingSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.resultsSub$).ifPresent(Disposable::dispose);
    }
}
