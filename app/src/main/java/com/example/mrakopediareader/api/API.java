package com.example.mrakopediareader.api;

import android.content.res.Resources;

import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mrakopediareader.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class API {
    // Three minutes for super-long queries
    private static int TIMEOUT = 1000 * 60 * 3;

    private static String KEY_TITLE = "title";
    private static String KEY_URL = "url";
    private static Throwable PARSE_ERROR = new Throwable("Failed to parse results");

    private final RequestQueue requestQueue;

    private final String searchURL;
    private final String randomURL;
    private final String apiURL;
    private final String categoriesUrl;
    private final String hotmUrl;

    public API(Resources resources, RequestQueue requestQueue) {
        final String apiURL = resources.getString(R.string.api_url);
        final String searchPath = resources.getString(R.string.api_search_path);
        final String randomPath = resources.getString(R.string.api_get_random_page);
        final String categoriesPath = resources.getString(R.string.api_get_categories);
        final String hotmPath = resources.getString(R.string.api_get_hotm);
        this.apiURL = apiURL;
        this.searchURL = apiURL + searchPath;
        this.randomURL = apiURL + randomPath;
        this.categoriesUrl = apiURL + categoriesPath;
        this.hotmUrl = apiURL + hotmPath;

        this.requestQueue = requestQueue;
    }

    private void queueRequest(JsonArrayRequest request) {
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return TIMEOUT;
            }

            @Override
            public int getCurrentRetryCount() {
                return TIMEOUT;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {}
        });
        this.requestQueue.add(request);
    }

    private void queueRequest(JsonObjectRequest request) {
        this.requestQueue.add(request);
    }

    private static Page jsonObjectToPage(JSONObject object) throws JSONException {
        final String title = object.getString(KEY_TITLE);
        final String url = object.getString(KEY_URL);
        return new Page(title, url);
    }

    public String getFullPagePath(String pagePath) {
        return apiURL + pagePath;
    }

    public Observable<ArrayList<Category>> getCategories() {
        return Observable.create((resolver) -> {
            queueRequest(new JsonArrayRequest(
                    this.categoriesUrl,
                    (result) -> {
                        final ArrayList<Category> categories = new ArrayList<>(result.length());
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                final JSONObject object = result.getJSONObject(i);
                                final String title = object.getString(KEY_TITLE);
                                final String url = object.getString(KEY_URL);
                                categories.add(new Category(title, url));
                            } catch (JSONException e) {
                                resolver.onError(PARSE_ERROR);
                                resolver.onComplete();
                                return;
                            }
                        }
                        resolver.onNext(categories);
                        resolver.onComplete();
                    },
                    ((error) -> {
                        resolver.onError(new Throwable("Failed to fetch categories"));
                        resolver.onComplete();
                    })
            ));
        });
    }

    public Observable<Page> getRandomPage() {
        return Observable.create((resolver) -> {
            queueRequest(new JsonObjectRequest(
                    this.randomURL,
                    null,
                    (result) -> {
                        try {
                            final String title = result.getString(KEY_TITLE);
                            final String url = result.getString(KEY_URL);
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

    public Observable<ArrayList<Page>> getPagesByCategory(
            String categoryName
    ) {
        return Observable.create((resolver) -> {
            queueRequest(new JsonArrayRequest(
                    this.categoriesUrl + '/' + categoryName,
                    (result) -> {
                        final ArrayList<Page> results = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                results.add(jsonObjectToPage(result.getJSONObject(i)));
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
                    }
            ));
        });
    }

    public Observable<ArrayList<Page>> getHOTM() {
        return Observable.create((resolver) -> {
            queueRequest(new JsonArrayRequest(
                    this.hotmUrl,
                    (results) -> {
                        final ArrayList<Page> hotmPages = new ArrayList<>(results.length());
                        for (int i = 0; i < results.length(); i++) {
                            try {
                                hotmPages.add(jsonObjectToPage(results.getJSONObject(i)));
                            } catch (JSONException e) {
                                resolver.onError(PARSE_ERROR);
                                resolver.onComplete();
                                return;
                            }
                        }
                        resolver.onNext(hotmPages);
                        resolver.onComplete();
                    },
                    (error) -> {
                        resolver.onError(new Throwable("Failed to fetch histories of the month"));
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
                        final ArrayList<Page> results = new ArrayList<>(result.length());
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                results.add(jsonObjectToPage(result.getJSONObject(i)));
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
