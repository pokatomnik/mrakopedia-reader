package com.example.mrakopediareader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.util.Optional;

import io.reactivex.rxjava3.disposables.Disposable;

public class ViewPage extends AppCompatActivity {
    @Nullable
    private Disposable loadingSub$;

    private FavoritesStore favoritesStore;
    private String pageUrl;
    private String pageTitle;
    private String pagePath;

    private Menu menu;

    private void handleLoadingState(boolean isLoading) {
        final WebView webView = findViewById(R.id.web_view);
        final ProgressBar progressBar = findViewById(R.id.pageLoadingProgressBar);
        if (isLoading) {
            webView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.favoritesStore = new FavoritesStore(getBaseContext());
        setContentView(R.layout.activity_view_page);

        Optional.ofNullable(getSupportActionBar()).ifPresent(((actionBar) -> {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }));

        final Resources resources = getResources();
        final Intent intent = getIntent();
        pageTitle = intent.getStringExtra(resources.getString(R.string.page_title));
        pagePath = intent.getStringExtra(resources.getString(R.string.page_path));
        pageUrl = getIntent().getStringExtra(resources.getString(R.string.pass_page_url));

        final WebView webView = findViewById(R.id.web_view);
        final MrakopediaWebViewClient webViewClient = new MrakopediaWebViewClient();
        this.loadingSub$ = webViewClient.getLoadingSubject().subscribe(this::handleLoadingState);
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setSupportZoom(false);
        webView.loadUrl(pageUrl);
    }

    private void toggleFavorite() {
        final MenuItem menuItem = menu.findItem(R.id.favorites);
        if (this.favoritesStore.has(this.pageTitle)) {
            this.favoritesStore.remove(this.pageTitle);
            menuItem.setTitle(R.string.add_to_to_favorites);
        } else {
            this.favoritesStore.put(this.pageTitle, this.pagePath);
            menuItem.setTitle(R.string.remove_from_favorites);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            this.onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.favorites) {
            this.toggleFavorite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_page_menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.favorites);

        if (favoritesStore.has(pageTitle)) {
            menuItem.setTitle(R.string.remove_from_favorites);
        } else {
            menuItem.setTitle(R.string.add_to_to_favorites);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.loadingSub$).ifPresent(Disposable::dispose);
    }
}
