package com.example.mrakopediareader.api;

import android.content.res.Resources;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.mrakopediareader.R;
import com.google.gson.JsonArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.function.Consumer;

public class API {
    private final String searchURL;
    private final String pageURL;

    public API(Resources resources) {
        final String apiURL = resources.getString(R.string.api_url);
        final String searchPath = resources.getString(R.string.api_search_path);
        final String pagePath = resources.getString(R.string.api_page_path);
        this.searchURL = apiURL + searchPath + "/";
        this.pageURL = apiURL + pagePath + "/";
    }

    public String getPageURL(String pageTitle) {
        return pageURL + pageTitle;
    }

    public JsonArrayRequest searchByText(
        String search,
        Consumer<ArrayList<Page>> pagesConsumer,
        Consumer<Exception> errorConsumer
    ) {
        return new JsonArrayRequest(this.searchURL + search, (result) -> {
            ArrayList<Page> results = new ArrayList<>();
            for (int i = 0; i < result.length(); i++) {
                try {
                    final JSONObject object = result.getJSONObject(i);
                    final String title = object.getString("title");
                    final String url = object.getString("url");
                    results.add(new Page(title, url));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            pagesConsumer.accept(results);
        }, (error) -> {
            errorConsumer.accept(new Exception("Failed to fetch search results"));
        });
    }
}
