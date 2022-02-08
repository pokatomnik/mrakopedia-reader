package com.example.mrakopediareader.pageslist

import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Page

class PagesListViewHolder(
    private val layout: LinearLayout,
    onClick: (page: Page) -> Unit,
    private val getReadingTimeText: (title: String) -> String
) :
    RecyclerView.ViewHolder(layout) {
    private var page: Page? = null

    fun setPage(pageToSet: Page) {
        page = pageToSet
        val readingTimeText = getReadingTimeText(pageToSet.title)

        val textView = layout.findViewById<TextView>(R.id.pageTitle)
        val readingTimeTextView = layout.findViewById<TextView>(R.id.readingTime)

        textView.text = pageToSet.title
        readingTimeTextView.text = readingTimeText
    }

    init {
        layout.setOnClickListener { page?.apply(onClick) }
    }
}