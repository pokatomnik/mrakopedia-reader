package com.example.mrakopediareader.api

class APIURLsImpl : APIURLs {
    companion object {
        const val API = "api"
        const val PAGE = "page"

        fun joinPaths(vararg paths: String): String {
            val joiner = "/"
            return paths.joinToString(joiner) {
                StringBuilder(it).removePrefix(joiner).removeSuffix(joiner).toString()
            }
        }
    }

    private fun apiURL(): String {
        return "https://mrakopedia.tk"
    }

    override fun searchURL(searchText: String): String {
        return joinPaths(apiURL(), API, "search", searchText)
    }

    override fun randomURL(): String {
        return joinPaths(apiURL(), API, "random")
    }

    override fun categoriesUrl(): String {
        return joinPaths(apiURL(), API, "categories")
    }

    override fun hotmUrl(): String {
        return joinPaths(apiURL(), API, "hotm")
    }

    override fun pagesOfCategory(categoryName: String): String {
        return joinPaths(categoriesUrl(), categoryName)
    }

    override fun pageSource(pageName: String): String {
        return joinPaths(apiURL(), API, PAGE, pageName, "source")
    }

    override fun pageCategories(pageName: String): String {
        return joinPaths(apiURL(), API, PAGE, pageName, "categories")
    }

    override fun pageRelated(pageName: String): String {
        return joinPaths(apiURL(), API, PAGE, pageName, "related")
    }

    override fun fullPagePath(relativePagePath: String): String {
        return joinPaths(apiURL(), relativePagePath)
    }

    override fun pagesMetaInfo(): String {
        return joinPaths(apiURL(), "static", "files", "pages-index.json")
    }
}

