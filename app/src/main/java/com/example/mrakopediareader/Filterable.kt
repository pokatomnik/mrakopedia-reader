package com.example.mrakopediareader

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class Filterable<S, T>(initial: S, private val filterFn: (search: S, value: T) -> Boolean) {
    private val search: BehaviorSubject<S> = BehaviorSubject.createDefault(initial)

    val searchValue: S?
        get() = search.value

    private val source: BehaviorSubject<Collection<T>> = BehaviorSubject.createDefault(listOf())

    val items: Collection<T>?
        get() = source.value

    val searchResultSubj: Observable<Collection<T>> =
        Observable.combineLatest(search, source) { search, source ->
            source.filter { filterFn(search, it) }
        }

    fun updateSource(newValue: Collection<T>) {
        source.onNext(newValue)
    }

    fun updateSearch(newSearch: S) {
        search.onNext(newSearch)
    }
}