package com.example.mrakopediareader.api;

import android.content.res.Resources;

import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.parser.CategoryParser;
import com.example.mrakopediareader.api.parser.PageParser;
import com.example.mrakopediareader.api.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class API extends Queue {
    private Parser<Page> pageParser = new PageParser();
    private Parser<Category> categoryParser = new CategoryParser();

    private static Throwable PARSE_ERROR = new Throwable("Failed to parse results");

    private final String searchURL;
    private final String randomURL;
    private final String apiURL;
    private final String categoriesUrl;
    private final String hotmUrl;

    public API(Resources resources, RequestQueue requestQueue) {
        super(requestQueue);
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
    }

    public String getFullPagePath(String pagePath) {
        return apiURL + pagePath;
    }

    public Observable<ArrayList<Category>> getCategories() {
        return Observable.create((resolver) -> {
            jsonArrayRequest(
                this.categoriesUrl,
                (result) -> {
                    try {
                        resolver.onNext(categoryParser.fromJsonArray(result));
                        resolver.onComplete();
                    } catch (JSONException e) {
                        resolver.onError(PARSE_ERROR);
                        resolver.onComplete();
                    }
                },
                ((error) -> {
                    resolver.onError(new Throwable("Failed to fetch categories"));
                    resolver.onComplete();
                })
            );
        });
    }

    public Observable<Page> getRandomPage() {
        return Observable.create((resolver) -> {
            jsonObjectRequest(
                this.randomURL,
                (result) -> {
                    try {
                        resolver.onNext(pageParser.fromJsonObject(result));
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
            );

        });
    }

    public Observable<ArrayList<Page>> getPagesByCategory(String categoryName) {
        return Observable.create((resolver) -> {
            jsonArrayRequest(
                this.categoriesUrl + '/' + categoryName,
                (result) -> {
                    try {
                        resolver.onNext(pageParser.fromJsonArray(result));
                        resolver.onComplete();
                    } catch (JSONException e) {
                        resolver.onError(PARSE_ERROR);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable("Failed to fetch pages by category"));
                    resolver.onComplete();
                }
            );
        });
    }

    public Observable<ArrayList<Page>> getHOTM() {
        return Observable.create((resolver) -> {
            jsonArrayRequest(
                this.hotmUrl,
                (results) -> {
                    try {
                        resolver.onNext(pageParser.fromJsonArray(results));
                        resolver.onComplete();
                    } catch (JSONException e) {
                        resolver.onError(PARSE_ERROR);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable("Failed to fetch histories of the month"));
                    resolver.onComplete();
                }
            );
        });
    }

    public Observable<ArrayList<Page>> searchByText(String search) {
        return Observable.create((resolver) -> {
            jsonArrayRequest(
                this.searchURL + "/" + search,
                (result) -> {
                    try {
                        resolver.onNext(pageParser.fromJsonArray(result));
                        resolver.onComplete();
                    } catch (JSONException e) {
                        resolver.onError(PARSE_ERROR);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable("Failed to fetch search results"));
                    resolver.onComplete();
                }
            );
        });
    }
}
