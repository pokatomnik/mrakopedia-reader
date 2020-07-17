package com.example.mrakopediareader;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.reactivex.rxjava3.subjects.PublishSubject;

class MrakopediaWebViewClient extends WebViewClient {
    private PublishSubject<Boolean> loadingSubject$ = PublishSubject.create();

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        this.loadingSubject$.onNext(true);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        this.loadingSubject$.onNext(false);
    }

    public PublishSubject<Boolean> getLoadingSubject() {
        return this.loadingSubject$;
    }
}
