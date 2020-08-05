package com.example.mrakopediareader.categorieslist;

import com.example.mrakopediareader.api.Category;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class AllCategories extends CategoriesList {
    @Override
    protected Observable<ArrayList<Category>> getCategories() {
        assert this.api != null;
        return this.api.getCategories();
    }
}
