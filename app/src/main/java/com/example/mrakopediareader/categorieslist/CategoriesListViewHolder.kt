package com.example.mrakopediareader.categorieslist

import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Category

class CategoriesListViewHolder(
    private val layout: LinearLayout,
    onClick: (category: Category) -> Unit
) : RecyclerView.ViewHolder(layout) {
    private val textView: TextView by lazy { layout.findViewById(R.id.categoryTitle) }

    private lateinit var category: Category

    fun setCategory(category: Category) {
        this.category = category
        textView.text = category.title
    }

    init {
        layout.setOnClickListener { onClick(category) }
    }
}