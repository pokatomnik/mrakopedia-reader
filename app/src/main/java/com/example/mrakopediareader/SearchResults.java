package com.example.mrakopediareader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.Page;

import java.util.ArrayList;

public class SearchResults extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RequestQueue requestQueue;
    private API api;
    private ArrayList<Page> searchResults = new ArrayList<>();

    private void handleResults(ArrayList<Page> newResults) {
        this.searchResults.clear();
        this.searchResults.addAll(newResults);
        this.mAdapter.notifyDataSetChanged();
    }

    private void handleError(Exception ignored) {
        final Context context = getApplicationContext();
        String text = getResources().getString(R.string.failed_search_message);
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void handleClick(Page page) {
        page.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        recyclerView = findViewById(R.id.searchResultsView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new PageResultsAdapter(this.searchResults, this::handleClick);
        recyclerView.setAdapter(mAdapter);

        this.requestQueue = Volley.newRequestQueue(this);
        this.api = new API(getResources());

        final String searchText = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_search_string_intent_key));

        // TODO handle if the searchText is null or empty

        final JsonArrayRequest request = api
                .searchByText(searchText, this::handleResults, this::handleError);

        requestQueue.add(request);
    }
}
