package com.example.mrakopediareader.api.dto;

public class Page {
    private final String title;

    private final String url;

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