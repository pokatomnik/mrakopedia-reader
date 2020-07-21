package com.example.mrakopediareader;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.mrakopediareader.api.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class FavoritesStore {
    private SharedPreferences sharedPreferences;

    public FavoritesStore(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Nullable
    public String get(String name) {
        return this.sharedPreferences.getString(name, null);
    }

    public void remove(String name) {
        this.sharedPreferences.edit().remove(name).apply();
    }

    public void put(String name, String value) {
        this.sharedPreferences.edit().putString(name, value).apply();
    }

    public Collection<Page> getPages() {
        final Map<String, ?> titleUrlMap = this.sharedPreferences.getAll();
        return titleUrlMap.keySet().stream()
                .map((title) -> {
                    return new Page(
                        title,
                        Optional
                            .ofNullable(titleUrlMap.get(title))
                            .map(Object::toString)
                            .orElse(null)
                    );
                })
                .filter((page) -> !Objects.isNull(page.getUrl()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean has(String name) {
        return !Objects.isNull(this.sharedPreferences.getString(name, null));
    }
}
