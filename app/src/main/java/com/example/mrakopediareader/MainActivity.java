package com.example.mrakopediareader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Optional;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {
    private BehaviorSubject<String> subj$;

    private Button searchButton;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            MainActivity.this.subj$.onNext(editable.toString());
        }
    };

    @Nullable
    private Disposable sub$;

    private void handleSearchStringChange(String newSearchString) {
        searchButton.setEnabled(!newSearchString.trim().isEmpty());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.searchButton = findViewById(R.id.searchButton);
        EditText editText = findViewById(R.id.searchText);

        this.subj$ = BehaviorSubject.createDefault("");
        this.sub$ = this.subj$
                .distinctUntilChanged()
                .subscribe(this::handleSearchStringChange);

        editText.addTextChangedListener(this.textWatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(this.sub$).ifPresent(Disposable::dispose);
    }

    public void handleClick(View button) {
        final Intent intent = new Intent(getBaseContext(), SearchResults.class);

        intent.putExtra(
                getResources().getString(R.string.pass_search_string_intent_key),
                this.subj$.getValue()
        );
        startActivity(intent);
    }
}
