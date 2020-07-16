package com.example.mrakopediareader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ViewPage extends AppCompatActivity {
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
        webView.setWebViewClient(new MrakopediaWebViewClient(this::handleLoadingState));
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setSupportZoom(false);
        webView.loadUrl(pageUrl);
    }
}
