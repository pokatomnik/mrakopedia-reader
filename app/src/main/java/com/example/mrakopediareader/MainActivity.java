package com.example.mrakopediareader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleClick(View button) {
        final EditText searchText = findViewById(R.id.searchText);
        final String textToSearch = searchText.getText().toString();

        final Intent intent = new Intent(getBaseContext(), SearchResults.class);

        intent.putExtra(
                getResources().getString(R.string.pass_search_string_intent_key),
                textToSearch
        );
        startActivity(intent);
    }
}
