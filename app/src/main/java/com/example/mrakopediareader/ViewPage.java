package com.example.mrakopediareader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.categorieslist.CategoriesByPage;
import com.example.mrakopediareader.pageslist.RelatedList;
import com.example.mrakopediareader.linkshare.LinkShare;
import java.util.Optional;

import io.reactivex.rxjava3.disposables.Disposable;

public class ViewPage extends AppCompatActivity {
    private final LinkShare linkShare = new LinkShare();
    private final TextZoom textZoom = new TextZoom(100, 50, 200, 10);

    private ExternalLinks externalLinks;
    private API api;

    @Nullable
    private Disposable textZoomSub$;

    @Nullable
    private Disposable loadingSub$;

    @Nullable
    private Disposable linkShareSub$;

    @Nullable
    private Disposable mrakopediaUrlSub$;

    private FavoritesStore favoritesStore;
    private String pageUrl;
    private String pageTitle;
    private String pagePath;

    private Menu menu;
    private WebView webView;

    private void handleLoadingState(boolean isLoading) {
        final ProgressBar progressBar = findViewById(R.id.pageLoadingProgressBar);
        if (isLoading) {
            webView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void resolveIntent() {
        final Resources resources = getResources();
        final Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Uri uriNullable = intent.getData();
            Optional.ofNullable(uriNullable).ifPresent((uri) -> {
                pageTitle = uri.getPathSegments().get(2);
                pagePath = uri.getEncodedPath();
                pageUrl = uri.toString();
            });
        } else {
            pageTitle = intent.getStringExtra(resources.getString(R.string.pass_page_title));
            pagePath = intent.getStringExtra(resources.getString(R.string.pass_page_path));
            pageUrl = getIntent().getStringExtra(resources.getString(R.string.pass_page_url));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);

        this.api = new API(getResources(), Volley.newRequestQueue(getBaseContext()));
        this.externalLinks = new ExternalLinks(getResources());

        this.favoritesStore = new FavoritesStore(getBaseContext());
        webView = findViewById(R.id.web_view);

        Optional.ofNullable(getSupportActionBar()).ifPresent(((actionBar) -> {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }));

        resolveIntent();

        this.textZoomSub$ = this.textZoom.getObservable().subscribe(this::setZoom);
        final MrakopediaWebViewClient webViewClient = new MrakopediaWebViewClient();
        this.loadingSub$ = webViewClient.getLoadingSubject().subscribe(this::handleLoadingState);
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setSupportZoom(false);
        webView.loadUrl(pageUrl);

        this.linkShareSub$ = this.linkShare.observeShare()
                .subscribe(this::handleShare, this::handleShareFailed);
    }

    private void handleShare(Intent intent) {
        startActivity(intent);
    }

    private void handleShareFailed(Throwable ignored) {
        final String errorText = getResources().getString(R.string.notification_share_error);
        final Toast toast = Toast.makeText(getBaseContext(), errorText, Toast.LENGTH_LONG);
        toast.show();
    }

    private void setZoom(int zoomValue) {
        webView.getSettings().setTextZoom(zoomValue);
    }

    private void toggleFavorite() {
        final MenuItem menuItem = menu.findItem(R.id.favorites);
        if (this.favoritesStore.has(this.pageTitle)) {
            this.favoritesStore.remove(this.pageTitle);
            menuItem.setTitle(R.string.ui_add_to_to_favorites);
            menuItem.setIcon(R.drawable.ic_fav_unselected);
        } else {
            this.favoritesStore.put(this.pageTitle, this.pagePath);
            menuItem.setTitle(R.string.ui_remove_from_favorites);
            menuItem.setIcon(R.drawable.ic_fav_selected);
        }
    }

    private void openRelated() {
        final Intent intent = new Intent(getBaseContext(), RelatedList.class);
        intent.putExtra(getResources().getString(R.string.pass_page_title), this.pageTitle);
        startActivity(intent);
    }

    private void openCategories() {
        final Intent intent = new Intent(getBaseContext(), CategoriesByPage.class);
        intent.putExtra(getResources().getString(R.string.pass_page_title), this.pageTitle);
        startActivity(intent);
    }

    private void openMrakopedia() {
        this.mrakopediaUrlSub$ = api.getWebsiteUrlForPage(this.pageTitle)
            .subscribe(
                (result) -> {
                    final Intent intent = externalLinks.openWebsiteUrl(result.getUri());
                    startActivity(intent);
                },
                (err) -> {
                    final String errorText = getResources()
                            .getString(R.string.notification_get_mrakopedia_website_url_error_message);
                    final Toast toast = Toast
                            .makeText(getBaseContext(), errorText, Toast.LENGTH_LONG);
                    toast.show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                this.onBackPressed();
                return true;
            case R.id.favorites:
                this.toggleFavorite();
                return true;
            case R.id.zoom_in:
                this.textZoom.zoomIn();
                return true;
            case R.id.zoom_out:
                this.textZoom.zoomOut();
                return true;
            case R.id.reset_zoom:
                this.textZoom.reset();
                return true;
            case R.id.share:
                this.linkShare.share(pageTitle, pageUrl);
                return true;
            case R.id.related:
                this.openRelated();
                return true;
            case R.id.categories:
                this.openCategories();
                return true;
            case R.id.open_mrakopedia:
                this.openMrakopedia();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_page_menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.favorites);

        if (favoritesStore.has(pageTitle)) {
            menuItem.setTitle(R.string.ui_remove_from_favorites);
            menuItem.setIcon(R.drawable.ic_fav_selected);
        } else {
            menuItem.setTitle(R.string.ui_add_to_to_favorites);
            menuItem.setIcon(R.drawable.ic_fav_unselected);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.loadingSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.textZoomSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.linkShareSub$).ifPresent(Disposable::dispose);
        Optional.ofNullable(this.mrakopediaUrlSub$).ifPresent(Disposable::dispose);
    }
}
