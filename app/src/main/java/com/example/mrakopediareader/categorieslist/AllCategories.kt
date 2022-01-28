package com.example.mrakopediareader.categorieslist

import com.example.mrakopediareader.api.dto.Category
import io.reactivex.rxjava3.core.Observable

class AllCategories : CategoriesList() {
    override fun getCategories(): Observable<List<Category>> {
        return api.getCategories()
    }
}