package com.example.mrakopediareader.categorieslist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Category

class CategoriesAdapter(
    private val mDataset: List<Category>,
    private val onClick: (category: Category) -> Unit
) : RecyclerView.Adapter<CategoriesListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriesListViewHolder {
        val layout: LinearLayout? = LayoutInflater.from(parent.context)
            .inflate(R.layout.categories_view, parent, false) as? LinearLayout

         return layout?.let { CategoriesListViewHolder(it, onClick) } ?: throw Error("No LinearLayout")
    }

    override fun onBindViewHolder(holder: CategoriesListViewHolder, position: Int) {
        val category = mDataset[position]
        holder.setCategory(category)
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }
}