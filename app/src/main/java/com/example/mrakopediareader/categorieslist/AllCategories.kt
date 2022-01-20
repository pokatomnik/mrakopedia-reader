package com.example.mrakopediareader.categorieslist

import com.example.mrakopediareader.api.dto.Category
import io.reactivex.rxjava3.core.Observable
import java.util.*

class AllCategories : CategoriesList() {
    override fun getCategories(): Observable<ArrayList<Category>> {
        assert(api != null)
        return api!!.getCategories()
    }
}