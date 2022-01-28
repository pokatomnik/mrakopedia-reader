package com.example.mrakopediareader.categorieslist

import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Category
import io.reactivex.rxjava3.core.Observable

class CategoriesByPage : CategoriesList() {
    override fun getCategories(): Observable<List<Category>> {
        val pageTitle = intent.getStringExtra(resources.getString(R.string.pass_page_title))
        return if (pageTitle == null) {
            Observable.error(Throwable("No page title"))
        } else {
            api.getCategoryByPage(pageTitle)
        }
    }
}