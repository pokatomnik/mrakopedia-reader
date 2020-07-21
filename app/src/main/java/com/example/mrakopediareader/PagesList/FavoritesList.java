package com.example.mrakopediareader.PagesList;

import com.example.mrakopediareader.FavoritesStore;
import com.example.mrakopediareader.api.Page;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class FavoritesList extends PagesList {
    @Override
    protected Observable<ArrayList<Page>> getPages() {
        final FavoritesStore favoritesStore = new FavoritesStore(getBaseContext());
        final ArrayList<Page> pages = new ArrayList<>(favoritesStore.getPages());
        return Observable.just(pages);
    }
}
