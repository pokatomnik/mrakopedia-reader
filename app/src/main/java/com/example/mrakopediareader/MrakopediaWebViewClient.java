package com.example.mrakopediareader;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.function.Consumer;

class MrakopediaWebViewClient extends WebViewClient {
    private Consumer<Boolean> loadingConsumer;

    MrakopediaWebViewClient(Consumer<Boolean> loadingConsumer) {
        this.loadingConsumer = loadingConsumer;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        this.loadingConsumer.accept(true);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        this.loadingConsumer.accept(false);
    }
}
