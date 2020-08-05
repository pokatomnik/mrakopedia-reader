package com.example.mrakopediareader.categorieslist;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.Category;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class CategoriesByPage extends CategoriesList {
    @Override
    protected Observable<ArrayList<Category>> getCategories() {
        assert api != null;
        final String pageTitle = getIntent()
                .getStringExtra(getResources().getString(R.string.pass_page_title));
        return api.getCategoryByPage(pageTitle);
    }
}
