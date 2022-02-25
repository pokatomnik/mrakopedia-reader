package com.example.mrakopediareader.viewpage

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

internal class MrakopediaWebViewClient(
    private val onStartLoading: () -> Unit,
    private val onFinishLoading: () -> Unit
) : WebViewClient() {
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onStartLoading()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        view?.apply {
            val htmlData = """
                <!doctype html>
                <html>
                  <body>
                    <div style="height: 100vh; display: flex; align-items: center; justify-content: center">
                      Невозможно загрузить страницу :(
                    </div>
                  </body>
                </html>
            """.trimIndent()
            loadUrl("about:blank")
            loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
            invalidate()
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onFinishLoading()
    }
}