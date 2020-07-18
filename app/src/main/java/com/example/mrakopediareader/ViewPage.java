package com.example.mrakopediareader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.util.Optional;

import io.reactivex.rxjava3.disposables.Disposable;

public class ViewPage extends AppCompatActivity {
    @Nullable
    private Disposable loadingSub$;

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
        setContentView(R.layout.activity_view_page);

        Optional.ofNullable(getSupportActionBar()).ifPresent(((actionBar) -> {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }));

        final String pageUrl = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_page_url));
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.loadingSub$).ifPresent(Disposable::dispose);
    }
}
