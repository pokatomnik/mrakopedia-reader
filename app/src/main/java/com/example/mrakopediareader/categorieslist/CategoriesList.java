package com.example.mrakopediareader.categorieslist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.Filterable;
import com.example.mrakopediareader.LoadingState;
import com.example.mrakopediareader.R;
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

abstract class CategoriesList extends AppCompatActivity {
    private RecyclerView.Adapter<CategoriesAdapter.ViewHolder> mAdapter;

    @Nullable
    protected API api;

    private Filterable<String, Category> categoryFilter =
            new Filterable<>("", (search, category) -> {
                if (Strings.isNullOrEmpty(search)) {
                    return true;
                }
                return category.getTitle().toLowerCase().contains(search.toLowerCase());
            });

    private ArrayList<Category> filteredCategories = new ArrayList<>();

    private Subject<LoadingState> loadingSubj$ = PublishSubject.create();

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

            final Context context = getApplicationContext();
            final String text = getResources().getString(R.string.failed_categories);
            final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
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

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                categoryFilter.updateSearch(editable.toString());
            }
        });

        this.loadingSub$ = this.loadingSubj$.subscribe(this::manageVisibility);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CategoriesAdapter(this.filteredCategories, this::handleClick);
        recyclerView.setAdapter(mAdapter);

        this.api = new API(getResources(), Volley.newRequestQueue(this));

        this.categoryFilterSub$ = this.categoryFilter
                .getSearchResultSubj$()
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