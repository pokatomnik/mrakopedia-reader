package com.example.mrakopediareader.api

import android.content.Context
import com.example.mrakopediareader.api.dto.Category
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.api.dto.PageMetaInfo
import com.example.mrakopediareader.api.dto.WebsiteUrl
import com.example.mrakopediareader.api.parser.*
import io.reactivex.rxjava3.core.Observable

class API(context: Context) {
    private val queue: Queue = Queue(context)
    private val pageParser: Parser<Page> = PageParser()
    private val categoryParser: Parser<Category> = CategoryParser()
    private val websiteUrlParser: Parser<WebsiteUrl> = WebsiteURLParser()
    private val pageMetainfoParser: Parser<Map<String, PageMetaInfo>> = PageMetaInfoParser()
    private val apiURLs: APIURLs = APIURLsImpl()

    fun getFullPagePath(pagePath: String): String =
        apiURLs.fullPagePath(pagePath)

    fun getWebsiteUrlForPage(pageTitle: String): Observable<WebsiteUrl> =
        queue.jsonObjectRequestGET(apiURLs.pageSource(pageTitle))
            .map(websiteUrlParser::fromJsonObject)

    fun getCategoryByPage(pageTitle: String): Observable<List<Category>> =
        queue.jsonArrayRequestGET(apiURLs.pageCategories(pageTitle))
            .map(categoryParser::fromJsonArray)

    fun getCategories(): Observable<List<Category>> =
        queue.jsonArrayRequestGET(apiURLs.categoriesUrl())
            .map(categoryParser::fromJsonArray)

    fun getRandomPage(): Observable<Page> =
        queue.jsonObjectRequestGET(apiURLs.randomURL())
            .map(pageParser::fromJsonObject)

    fun getPagesByCategory(categoryName: String): Observable<List<Page>> =
        queue.jsonArrayRequestGET(apiURLs.pagesOfCategory(categoryName))
            .map(pageParser::fromJsonArray)

    fun getHOTM(): Observable<List<Page>> =
        queue.jsonArrayRequestGET(apiURLs.hotmUrl())
            .map(pageParser::fromJsonArray)

    fun searchByText(search: String): Observable<List<Page>> =
        queue.jsonArrayRequestGET(apiURLs.searchURL(search))
            .map(pageParser::fromJsonArray)

    fun getRelatedPages(pageTitle: String): Observable<List<Page>> =
        queue.jsonArrayRequestGET(apiURLs.pageRelated(pageTitle))
            .map(pageParser::fromJsonArray)

    fun getPagesMetaInfoIndex(): Observable<Map<String, PageMetaInfo>> =
        queue.jsonObjectRequestGET(apiURLs.pagesMetaInfo())
            .map(pageMetainfoParser::fromJsonObject)
}