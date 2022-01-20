package com.example.mrakopediareader.api

interface APIURLs {
    /**
     * The API endpoint for searching
     */
    fun searchURL(searchText: String): String

    /**
     * The API endpoint for random page
     */
    fun randomURL(): String

    /**
     * The API endpoint for all categories in the Mrakopedia
     */
    fun categoriesUrl(): String

    /**
     * The API endpoint for stories of the month
     */
    fun hotmUrl(): String

    /**
     * The API endpoint for pages by a category
     */
    fun pagesOfCategory(categoryName: String): String

    /**
     * The API endpoint for source of a particular page
     */
    fun pageSource(pageName: String): String

    /**
     * The API endpoint for categories of a particular page
     */
    fun pageCategories(pageName: String): String

    /**
     * The API endpoint for related pages of a particular page
     */
    fun pageRelated(pageName: String): String

    /**
     * The API endpoint for a particular page.
     * Based on the relative URL of the page, not Its title, beware.
     */
    fun fullPagePath(relativePagePath: String): String
}