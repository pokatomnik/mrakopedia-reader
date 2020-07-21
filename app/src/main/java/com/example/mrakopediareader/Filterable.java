package com.example.mrakopediareader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class Filterable<S, T> {
    private final BiFunction<S, T, Boolean> filterFn;

    private final BehaviorSubject<Collection<T>> sourceSubj$;

    private final BehaviorSubject<S> searchSubj$;

    private final Observable<Collection<T>> result$;

    public Filterable(S initial, BiFunction<S, T, Boolean> filter) {
        this.filterFn = filter;

        this.searchSubj$ = BehaviorSubject.createDefault(initial);
        this.sourceSubj$ = BehaviorSubject.createDefault(new ArrayList<>());

        this.result$ = Observable.combineLatest(this.searchSubj$, this.sourceSubj$, (search, source) -> {
            return source.stream().filter((value) -> {
                return this.filterFn.apply(search, value);
            }).collect(Collectors.toList());
        });
    }

    public void updateSource(Collection<T> newValue) {
        this.sourceSubj$.onNext(newValue);
    }

    public void updateSearch(S search) {
        this.searchSubj$.onNext(search);
    }

    public Observable<Collection<T>> getSearchResultSubj$() {
        return this.result$;
    }
}
