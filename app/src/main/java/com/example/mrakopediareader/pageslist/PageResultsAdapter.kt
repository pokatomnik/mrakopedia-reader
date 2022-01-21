package com.example.mrakopediareader.pageslist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Page

internal class PageResultsAdapter(
    private val pages: List<Page>,
    private val onClick: (page: Page) -> Unit
) : RecyclerView.Adapter<PagesListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PagesListViewHolder {
        val layout: LinearLayout? = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_view, parent, false) as? LinearLayout
        return layout?.let { PagesListViewHolder(it, onClick) } ?: throw Error("No Linear Layout")
    }

    override fun onBindViewHolder(holder: PagesListViewHolder, position: Int) {
        val page = pages[position]
        holder.setPage(page)
    }

    override fun getItemCount(): Int {
        return pages.size
    }
}