package com.example.mrakopediareader.viewpage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView

class MRWebView : WebView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        settings.javaScriptEnabled = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)
        settings.setSupportZoom(false)
    }

    private val textZoom = TextZoom(100, 50, 200, 10) {
        settings.textZoom = it
    }

    val maxScrollY: Int
        get() {
            val computedScrollRange = computeVerticalScrollRange()
            val height = height

            return computedScrollRange - height
        }

    fun show() {
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.INVISIBLE
    }

    fun pageZoomIn() {
        this.textZoom.zoomIn()
    }

    fun pageZoomOut() {
        this.textZoom.zoomOut()
    }

    fun resetZoom() {
        this.textZoom.reset()
    }

    fun scrollTop() {
        scrollY = 0
    }
}