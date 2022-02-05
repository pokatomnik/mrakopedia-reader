package com.example.mrakopediareader.viewpage

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class MRWebView : WebView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    val maxScrollY: Int
        get() {
            val computedScrollRange = computeVerticalScrollRange()
            val height = height;

            return computedScrollRange - height
        }
}