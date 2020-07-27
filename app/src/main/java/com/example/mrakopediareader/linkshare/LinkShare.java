package com.example.mrakopediareader.linkshare;

import android.content.Intent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class LinkShare {
    private final PublishSubject<Intent> shareSubject$ = PublishSubject.create();

    public void share(String title, String url) {
        try {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            shareSubject$.onNext(Intent.createChooser(shareIntent, "Куда отправить"));
        } catch(Exception e) {
            shareSubject$.onError(new Throwable("Can't share this"));
        }
    }

    public Observable<Intent> observeShare() {
        return this.shareSubject$;
    }
}
