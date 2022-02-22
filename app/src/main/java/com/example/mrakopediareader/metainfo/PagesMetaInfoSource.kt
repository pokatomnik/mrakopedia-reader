package com.example.mrakopediareader.metainfo

import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.PageMetaInfo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class PagesMetaInfoSource(private val api: API) {
    private var metaInfoSubject: BehaviorSubject<Map<String, PageMetaInfo>> =
        BehaviorSubject.createDefault(LinkedHashMap())

    fun init(): PagesMetaInfoSource {
        api.getPagesMetaInfoIndex().onErrorReturn {
            LinkedHashMap()
        }.subscribe {
            metaInfoSubject.onNext(it)
        }

        return this
    }

    fun observePageTitles(): Observable<Set<String>> = metaInfoSubject.map { it.keys }

    fun getMetaInfoByPageTitle(title: String): PageMetaInfo? {
        return metaInfoSubject.value?.get(title)
    }
}