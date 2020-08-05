package com.example.mrakopediareader.pageslist;

import com.example.mrakopediareader.FavoritesStore;
import com.example.mrakopediareader.api.dto.Page;

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Observable;

public class FavoritesList extends PagesList {
    @Override
    protected Observable<ArrayList<Page>> getPages() {
        final FavoritesStore favoritesStore = new FavoritesStore(getBaseContext());
        final ArrayList<Page> pagesSorted = favoritesStore
                .getPages()
                .stream()
                .sorted((page1, page2) -> page1.getTitle().compareTo(page2.getTitle()))
                .collect(Collectors.toCollection(ArrayList::new));
        return Observable.just(pagesSorted);
    }
}
