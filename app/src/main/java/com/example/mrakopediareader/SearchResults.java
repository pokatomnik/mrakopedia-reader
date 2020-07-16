package com.example.mrakopediareader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.Page;
import com.google.common.net.UrlEscapers;

import java.util.ArrayList;
import java.util.Optional;

public class SearchResults extends AppCompatActivity {
    private RecyclerView.Adapter mAdapter;
    private RequestQueue requestQueue;
    private API api;
    private ArrayList<Page> searchResults = new ArrayList<>();

    private void setLoading(boolean isLoading) {
        final RecyclerView recyclerView = findViewById(R.id.searchResultsView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        if (isLoading) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void handleResults(ArrayList<Page> newResults) {
        this.searchResults.clear();
        this.searchResults.addAll(newResults);
        this.mAdapter.notifyDataSetChanged();
        setLoading(false);
    }

    private void handleError(Exception ignored) {
        final Context context = getApplicationContext();
        String text = getResources().getString(R.string.failed_search_message);
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
        setLoading(false);
    }

    private void handleClick(Page page) {
        final Intent intent = new Intent(getBaseContext(), ViewPage.class);
        intent.putExtra(
                getResources().getString(R.string.pass_page_url),
                this.api.getFullPagePath(page.getUrl())
        );
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        RecyclerView recyclerView = findViewById(R.id.searchResultsView);

        setLoading(true);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new PageResultsAdapter(this.searchResults, this::handleClick);
        recyclerView.setAdapter(mAdapter);

        this.requestQueue = Volley.newRequestQueue(this);
        this.api = new API(getResources());

        final String nullableSearchText = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_search_string_intent_key));

        Optional.ofNullable(nullableSearchText).ifPresent(searchText -> {
            final String encoded = UrlEscapers.urlPathSegmentEscaper().escape(searchText);
            final JsonArrayRequest request = api
                    .searchByText(encoded, this::handleResults, this::handleError);

            requestQueue.add(request);
        });
    }
}
