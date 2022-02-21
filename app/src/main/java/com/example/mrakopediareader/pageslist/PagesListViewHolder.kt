package com.example.mrakopediareader.pageslist

import android.content.res.Resources
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.api.dto.PageMetaInfo

class PagesListViewHolder(
    private val layout: LinearLayout,
    private val resources: Resources,
    private val getPageMetaInfo: (title: String) -> PageMetaInfo?,
    private val onClick: (page: Page) -> Unit,
) : RecyclerView.ViewHolder(layout) {
    private fun getPageReadingTimeByTitle(title: String): String {
        val readingTimeTextUnknown = resources.getText(R.string.ui_reading_time_unknown)
        val readingTimeTextUnit = resources.getText(R.string.ui_reading_time_unit)
        val pageMetaInfo = getPageMetaInfo(title)
        return pageMetaInfo?.readableCharacters?.let { readableCharacters ->
            val minutes = readableCharacters / 1500
            return "$minutes $readingTimeTextUnit"
        } ?: "$readingTimeTextUnknown"
    }

    private fun getRatingText(title: String): String? {
        val rating = getPageMetaInfo(title)?.rating
        return rating?.let { "$rating%" }
    }

    private fun getVotedText(title: String): String? {
        val voted = getPageMetaInfo(title)?.voted
        return voted?.toString()
    }

    fun setPage(pageToSet: Page) {
        val readingTimeText = getPageReadingTimeByTitle(pageToSet.title)
        val ratingText = getRatingText(pageToSet.title)
        val votedText = getVotedText(pageToSet.title)

        val textView = layout.findViewById<TextView>(R.id.pageTitle)
        val readingTimeTextView = layout.findViewById<TextView>(R.id.readingTime)
        val ratingTextView = layout.findViewById<TextView>(R.id.rating)
        val votedTextView = layout.findViewById<TextView>(R.id.voted)

        layout.setOnClickListener { pageToSet.apply(onClick) }

        textView.text = pageToSet.title

        readingTimeTextView.text = readingTimeText
        if (ratingText == null) {
            ratingTextView.visibility = View.INVISIBLE
            ratingTextView.text = null
        } else {
            ratingTextView.visibility = View.VISIBLE
            ratingTextView.text = ratingText
        }
        if (votedText == null) {
            votedTextView.visibility = View.INVISIBLE
            votedTextView.text = null
        } else {
            votedTextView.visibility = View.VISIBLE
            votedTextView.text = votedText
        }
    }
}