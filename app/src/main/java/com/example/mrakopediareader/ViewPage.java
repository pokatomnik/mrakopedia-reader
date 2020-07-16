package com.example.mrakopediareader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    protected void onDestroy() {
        super.onDestroy();
        Optional.of(this.loadingSub$).ifPresent(Disposable::dispose);
    }
}
