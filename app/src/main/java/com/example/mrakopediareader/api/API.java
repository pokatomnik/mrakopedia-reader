package com.example.mrakopediareader.api;

import android.content.res.Resources;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mrakopediareader.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class API {
    private static String KEY_PAGE_TITLE = "title";
    private static String KEY_PAGE_URL = "url";
    private static Throwable PARSE_ERROR = new Throwable("Failed to parse results");

    private final RequestQueue requestQueue;

    private final String searchURL;
    private final String randomURL;
    private final String apiURL;

    private void queueRequest(JsonArrayRequest request) {
        this.requestQueue.add(request);
    }

    private void queueRequest(JsonObjectRequest request) {
        this.requestQueue.add(request);
    }

    public API(Resources resources, RequestQueue requestQueue) {
        final String apiURL = resources.getString(R.string.api_url);
        final String searchPath = resources.getString(R.string.api_search_path);
        final String randomPath = resources.getString(R.string.api_get_random_page);
        this.apiURL = apiURL;
        this.searchURL = apiURL + searchPath;
        this.randomURL = apiURL + randomPath;
        this.requestQueue = requestQueue;
    }

    public String getFullPagePath(String pagePath) {
        return apiURL + pagePath;
    }

    public Observable<Page> getRandomPage() {
        return Observable.create((resolver) -> {
            queueRequest(new JsonObjectRequest(
                    this.randomURL,
                    null,
                    (result) -> {
                        try {
                            final String title = result.getString(KEY_PAGE_TITLE);
                            final String url = result.getString(KEY_PAGE_URL);
                            resolver.onNext(new Page(title, url));
                            resolver.onComplete();
                        } catch (JSONException e) {
                            resolver.onError(PARSE_ERROR);
                            resolver.onComplete();
                        }
                    },
                    (error) -> {
                        resolver.onError(new Throwable("Failed to get random page"));
                        resolver.onComplete();
                    }
            ));

        });
    }

    public Observable<ArrayList<Page>> searchByText(
        String search
    ) {
        return Observable.create((resolver) -> {
            queueRequest(new JsonArrayRequest(
                    this.searchURL + "/" + search,
                    (result) -> {
                        ArrayList<Page> results = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                final JSONObject object = result.getJSONObject(i);
                                final String title = object.getString(KEY_PAGE_TITLE);
                                final String url = object.getString(KEY_PAGE_URL);
                                results.add(new Page(title, url));
                            } catch (JSONException e) {
                                resolver.onError(PARSE_ERROR);
                                resolver.onComplete();
                                return;
                            }
                        }
                        resolver.onNext(results);
                        resolver.onComplete();
                    },
                    (error) -> {
                        resolver.onError(new Throwable("Failed to fetch search results"));
                        resolver.onComplete();
                    }));
        });
    }
}
