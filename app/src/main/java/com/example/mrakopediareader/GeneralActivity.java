package com.example.mrakopediareader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.Page;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Optional;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class GeneralActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @Nullable
    private API api;

    private BehaviorSubject<String> inputSub$;

    private BehaviorSubject<Boolean> busynessSubj$ = BehaviorSubject.createDefault(false);

    private Button searchButton;

    @Nullable
    private Disposable randomPageSub$;

    @Nullable
    private Disposable businessSub$;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            GeneralActivity.this.inputSub$.onNext(editable.toString());
        }
    };

    @Nullable
    private Disposable searchStringChangeSub$;

    public void handleClick(View button) {
        final Intent intent = new Intent(getBaseContext(), SearchResults.class);

        intent.putExtra(
                getResources().getString(R.string.pass_search_string_intent_key),
                this.inputSub$.getValue()
        );
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        this.api = new API(getResources(), Volley.newRequestQueue(this));

        this.searchButton = findViewById(R.id.searchButton);
        EditText editText = findViewById(R.id.searchText);

        this.businessSub$ = this.busynessSubj$.subscribe(this::manageVisibility);
        this.inputSub$ = BehaviorSubject.createDefault("");
        this.searchStringChangeSub$ = this.inputSub$
                .distinctUntilChanged()
                .subscribe(this::handleSearchStringChange);

        editText.addTextChangedListener(this.textWatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.searchStringChangeSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.randomPageSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.businessSub$).ifPresent(Disposable::dispose);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void manageVisibility(boolean busy) {
        final LinearLayout generalLinearLayout = findViewById(R.id.generalLinearLayout);
        final ProgressBar generalProgressbar = findViewById(R.id.generalProgressBar);
        if (busy) {
            generalLinearLayout.setVisibility(View.INVISIBLE);
            generalProgressbar.setVisibility(View.VISIBLE);
        } else {
            generalLinearLayout.setVisibility(View.VISIBLE);
            generalProgressbar.setVisibility(View.INVISIBLE);
        }
    }

    private void handleSearchStringChange(String newSearchString) {
        searchButton.setEnabled(!newSearchString.trim().isEmpty());
    }

    private void handleGetRandomPageSuccess(Page page) {
        Optional.ofNullable(this.api).ifPresent((api) -> {
            final Intent intent = new Intent(getBaseContext(), ViewPage.class);
            intent.putExtra(
                    getResources().getString(R.string.pass_page_url),
                    api.getFullPagePath(page.getUrl())
            );
            startActivity(intent);
        });
    }

    private void handleGetRandomPageFailed(Throwable ignored) {
        final Context context = getApplicationContext();
        final String text = getResources().getString(R.string.failed_search_message);
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void openRandomPage() {
        Optional.ofNullable(this.api).ifPresent((api) -> {
            this.busynessSubj$.onNext(true);
            this.randomPageSub$ = this.api
                    .getRandomPage()
                    .doFinally(() -> {
                        this.busynessSubj$.onNext(false);
                    })
                    .subscribe(this::handleGetRandomPageSuccess, this::handleGetRandomPageFailed);
        });
    }

    private void openCategories() {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_random_page:
                this.openRandomPage();
                break;
            case R.id.nav_categories:
                this.openCategories();
                break;
            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}