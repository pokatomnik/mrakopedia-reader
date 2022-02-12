package com.example.mrakopediareader.metainfo

import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.PageMetaInfo

class PagesMetaInfoSource(private val api: API) {
    private var pagesMetaInfoIndex: Map<String, PageMetaInfo>? = null

    fun init(): PagesMetaInfoSource {
        api.getPagesMetaInfoIndex().subscribe ({
            pagesMetaInfoIndex = it
        }) {
            pagesMetaInfoIndex = LinkedHashMap()
        }

        return this
    }

    fun getMetaInfoByPageTitle(title: String): PageMetaInfo? {
        return pagesMetaInfoIndex?.get(title)
    }
}