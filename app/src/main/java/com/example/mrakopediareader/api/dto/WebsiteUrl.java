package com.example.mrakopediareader.api.dto;

import android.net.Uri;

public class WebsiteUrl {
    private final Uri uri;
    private final String title;

    public WebsiteUrl(String title, Uri uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }
}
