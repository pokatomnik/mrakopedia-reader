package com.example.mrakopediareader.pageslist;

import com.example.mrakopediareader.api.API;
import com.example.mrakopediareader.api.dto.Page;

import java.util.ArrayList;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;

public class HOTMList extends PagesList {
    @Override
    protected Observable<ArrayList<Page>> getPages() {
        return Optional
                .ofNullable(api)
                .map(API::getHOTM)
                .orElse(Observable.just(new ArrayList<>()));
    }
}
