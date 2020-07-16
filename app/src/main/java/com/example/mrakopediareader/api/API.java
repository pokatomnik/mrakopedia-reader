package com.example.mrakopediareader.api;

import android.content.res.Resources;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.mrakopediareader.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class API {
    private final RequestQueue requestQueue;

    private final String searchURL;
    private final String apiURL;

    public API(Resources resources, RequestQueue requestQueue) {
        final String apiURL = resources.getString(R.string.api_url);
        final String searchPath = resources.getString(R.string.api_search_path);
        this.apiURL = apiURL;
        this.searchURL = apiURL + searchPath;
        this.requestQueue = requestQueue;
    }

    public String getFullPagePath(String pagePath) {
        return apiURL + pagePath;
    }

    public Observable<ArrayList<Page>> searchByText(
        String search
    ) {
        return Observable.create((resolver) -> {
            final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    this.searchURL + "/" + search,
                    (result) -> {
                        ArrayList<Page> results = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                final JSONObject object = result.getJSONObject(i);
                                final String title = object.getString("title");
                                final String url = object.getString("url");
                                results.add(new Page(title, url));
                            } catch (JSONException e) {
                                resolver.onError(new Throwable("Failed to parse results"));
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
                    });
            API.this.requestQueue.add(jsonArrayRequest);
        });
    }
}
