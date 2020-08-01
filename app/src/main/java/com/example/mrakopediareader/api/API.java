package com.example.mrakopediareader.api;

import android.content.res.Resources;

import com.android.volley.RequestQueue;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.parser.CategoryParser;
import com.example.mrakopediareader.api.parser.PageParser;
import com.example.mrakopediareader.api.parser.Parser;

import org.json.JSONException;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class API extends Queue {
    private final Parser<Page> pageParser = new PageParser();
    private final Parser<Category> categoryParser = new CategoryParser();

    private final Throwable parseError;

    private final String searchURL;
    private final String randomURL;
    private final String apiURL;
    private final String categoriesUrl;
    private final String hotmUrl;
    private final String pageUrl;

    private Resources resources;

    public API(Resources resources, RequestQueue requestQueue) {
        super(requestQueue);
        this.resources = resources;
        this.parseError = new Throwable(resources.getString(R.string.error_parse));

        final String apiURL = resources.getString(R.string.api_url);
        final String searchPath = resources.getString(R.string.api_search_path);
        final String randomPath = resources.getString(R.string.api_get_random_page);
        final String categoriesPath = resources.getString(R.string.api_get_categories);
        final String hotmPath = resources.getString(R.string.api_get_hotm);
        final String pagePath = resources.getString(R.string.api_get_page);
        this.apiURL = apiURL;
        this.searchURL = apiURL + searchPath;
        this.randomURL = apiURL + randomPath;
        this.categoriesUrl = apiURL + categoriesPath;
        this.hotmUrl = apiURL + hotmPath;
        this.pageUrl = apiURL + pagePath;
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
                        resolver.onError(parseError);
                        resolver.onComplete();
                    }
                },
                ((error) -> {
                    resolver.onError(new Throwable(resources.getString(R.string.error_fetch_categories)));
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
                        resolver.onError(parseError);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable(resources.getString(R.string.error_fetch_random_page)));
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
                        resolver.onError(parseError);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable(resources.getString(R.string.error_fetch_pages_by_category)));
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
                        resolver.onError(parseError);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable(resources.getString(R.string.error_fetch_stories_of_month)));
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
                        resolver.onError(parseError);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable(resources.getString(R.string.error_fetch_search_results)));
                    resolver.onComplete();
                }
            );
        });
    }

    public Observable<ArrayList<Page>> getRelatedPages(String pageTitle) {
        return Observable.create((resolver) -> {
            jsonArrayRequest(
                this.pageUrl + "/" + pageTitle + "/related",
                (result) -> {
                    try {
                        resolver.onNext(pageParser.fromJsonArray(result));
                        resolver.onComplete();
                    } catch (JSONException e) {
                        resolver.onError(parseError);
                        resolver.onComplete();
                    }
                },
                (error) -> {
                    resolver.onError(new Throwable(resources.getString(R.string.error_fetch_related_pages)));
                    resolver.onComplete();
                }
            );
        });
    }
}
