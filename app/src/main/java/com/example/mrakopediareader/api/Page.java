package com.example.mrakopediareader.api;

public class Page {
    private String title;

    private String url;

    public Page(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}