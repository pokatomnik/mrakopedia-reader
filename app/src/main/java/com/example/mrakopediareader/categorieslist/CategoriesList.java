package com.example.mrakopediareader.categorieslist;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrakopediareader.Filterable;
import com.example.mrakopediareader.LoadingState;
import com.example.mrakopediareader.MRReaderApplication;
import com.example.mrakopediareader.R;
import com.example.mrakopediareader.SearchTextWatcher;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.dto.Category;
import com.example.mrakopediareader.pageslist.PagesByCategory;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public abstract class CategoriesList extends AppCompatActivity {
    @Nullable
    protected API api;

    private final Filterable<String, Category> categoryFilter =
            new Filterable<>("", (search, category) ->
                    Strings.isNullOrEmpty(search) ||
                            category.getTitle().toLowerCase().contains(search.toLowerCase())
            );

    private final ArrayList<Category> filteredCategories = new ArrayList<>();

    private final RecyclerView.Adapter<CategoriesListViewHolder> mAdapter
            = new CategoriesAdapter(
                    this.filteredCategories,
                    (category) -> { handleClick(category); return null; });

    private final Subject<LoadingState> loadingSubj$ = PublishSubject.create();

    @Nullable
    private Disposable resultsSub$;

    @Nullable
    private Disposable loadingSub$;

    @Nullable
    private Disposable categoryFilterSub$;

    protected abstract Observable<ArrayList<Category>> getCategories();

    private void manageVisibility(LoadingState loadingState) {
        final RecyclerView recyclerView = findViewById(R.id.categoriesView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView noItems = findViewById(R.id.noItems);
        final EditText searchBy = findViewById(R.id.categoriesSearchBy);
        if (loadingState == LoadingState.HAS_ERROR) {
            recyclerView.setVisibility(View.INVISIBLE);
            searchBy.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.INVISIBLE);

            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.notification_failed_categories),
                    Toast.LENGTH_LONG
            ).show();
        } else if (loadingState == LoadingState.LOADING) {
            recyclerView.setVisibility(View.INVISIBLE);
            searchBy.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            noItems.setVisibility(View.INVISIBLE);
        } else if (loadingState == LoadingState.EMPTY) {
            searchBy.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.VISIBLE);
        } else if (loadingState == LoadingState.HAS_RESULTS) {
            searchBy.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.INVISIBLE);
        }
    }

    private void updateFilteredResults(Collection<Category> newCategories) {
        this.filteredCategories.clear();
        this.filteredCategories.addAll(newCategories);
        this.mAdapter.notifyDataSetChanged();
    }

    private void handleError(Throwable ignored) {
        this.loadingSubj$.onNext(LoadingState.HAS_ERROR);
    }

    private void handleClick(Category category) {
        final Intent intent = new Intent(getBaseContext(), PagesByCategory.class);
        intent.putExtra(
                getResources().getString(R.string.pass_category_name),
                category.getTitle()
        );
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Optional.ofNullable(getSupportActionBar()).ifPresent(((actionBar) -> {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }));

        final RecyclerView recyclerView = findViewById(R.id.categoriesView);
        final EditText searchText = findViewById(R.id.categoriesSearchBy);

        searchText.addTextChangedListener(new SearchTextWatcher((text) -> {
            categoryFilter.updateSearch(text); return null;
        }));

        this.loadingSub$ = this.loadingSubj$.subscribe(this::manageVisibility);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        final MRReaderApplication application = (MRReaderApplication) getApplication();
        this.api = application.getApi();

        this.categoryFilterSub$ = this.categoryFilter
                .getSearchResultSubj()
                .subscribe(this::updateFilteredResults);
        this.loadingSubj$.onNext(LoadingState.LOADING);
        this.resultsSub$ = this.getCategories()
                .doOnNext((results) -> {
                    if (results.size() == 0) {
                        loadingSubj$.onNext(LoadingState.EMPTY);
                    } else {
                        loadingSubj$.onNext(LoadingState.HAS_RESULTS);
                    }
                })
                .subscribe((results) -> {
                    this.categoryFilter.updateSource(results);
                }, this::handleError);
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
        Optional.ofNullable(this.categoryFilterSub$).ifPresent(Disposable::dispose);
    }
}