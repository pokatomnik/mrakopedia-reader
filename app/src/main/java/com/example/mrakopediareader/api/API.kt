package com.example.mrakopediareader.api

import android.content.Context
import com.example.mrakopediareader.api.dto.Category
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.api.dto.WebsiteUrl
import com.example.mrakopediareader.api.parser.CategoryParser
import com.example.mrakopediareader.api.parser.PageParser
import com.example.mrakopediareader.api.parser.Parser
import com.example.mrakopediareader.api.parser.WebsiteURLParser
import io.reactivex.rxjava3.core.Observable
import java.util.*

class API(context: Context) {
    private val queue: Queue = Queue(context)
    private val pageParser: Parser<Page> = PageParser()
    private val categoryParser: Parser<Category> = CategoryParser()
    private val websiteUrlParser: Parser<WebsiteUrl> = WebsiteURLParser()
    private val apiURLs: APIURLs = APIURLsImpl()

    fun getFullPagePath(pagePath: String): String =
        apiURLs.fullPagePath(pagePath)

    fun getWebsiteUrlForPage(pageTitle: String): Observable<WebsiteUrl> =
        queue.jsonObjectRequestGET(apiURLs.pageSource(pageTitle))
            .map(websiteUrlParser::fromJsonObject)

    fun getCategoryByPage(pageTitle: String): Observable<ArrayList<Category>> =
        queue.jsonArrayRequestGET(apiURLs.pageCategories(pageTitle))
            .map(categoryParser::fromJsonArray)

    fun getCategories(): Observable<ArrayList<Category>> =
        queue.jsonArrayRequestGET(apiURLs.categoriesUrl())
            .map(categoryParser::fromJsonArray)

    fun getRandomPage(): Observable<Page> =
        queue.jsonObjectRequestGET(apiURLs.randomURL())
            .map(pageParser::fromJsonObject)

    fun getPagesByCategory(categoryName: String): Observable<ArrayList<Page>> =
        queue.jsonArrayRequestGET(apiURLs.pagesOfCategory(categoryName))
            .map(pageParser::fromJsonArray)

    fun getHOTM(): Observable<ArrayList<Page>> =
        queue.jsonArrayRequestGET(apiURLs.hotmUrl())
            .map(pageParser::fromJsonArray)

    fun searchByText(search: String): Observable<ArrayList<Page>> =
        queue.jsonArrayRequestGET(apiURLs.searchURL(search))
            .map(pageParser::fromJsonArray)

    fun getRelatedPages(pageTitle: String): Observable<ArrayList<Page>> =
        queue.jsonArrayRequestGET(apiURLs.pageRelated(pageTitle))
            .map(pageParser::fromJsonArray)

}