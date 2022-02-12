package com.example.mrakopediareader.viewpage

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPosition
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPositionDao
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

internal class MrakopediaWebViewClient(
    private val title: String,
    private val scrollPositionDao: ScrollPositionDao,
    private val scrollPublisher: BehaviorSubject<Int>,
    private val handleLoading: (isLoading: Boolean) -> Unit
) :
    WebViewClient(), Disposable {

    private var disposed = false

    private val scrollYSubject = PublishSubject.create<Int>()

    private val scrollPositionSubscription = scrollYSubject
        .observeOn(Schedulers.io())
        .debounce(1000, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .subscribe ({ scrollPositionDao.setPosition(ScrollPosition(title, it)) }) {}

    private val textZoom = TextZoom(100, 50, 200, 10) {
        mWebView?.settings?.textZoom = it
    }

    private var mWebView: MRWebView? = null

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        handleLoading(true)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        handleLoading(false)
        Observable
            .just(scrollPositionDao)
            .observeOn(Schedulers.io())
            .map { it.getPosition(title) ?: ScrollPosition(title, 0) }
            .subscribe ({ mWebView?.scrollY = it.position }) {}
    }

    fun attach(webView: MRWebView): MrakopediaWebViewClient {
        if (mWebView != null) {
            throw Error("Already attached")
        }

        mWebView = webView

        webView.scrollY = scrollPublisher.value ?: 0
        webView.setOnScrollChangeListener { _, _, _, _, _ ->
            scrollPublisher.onNext(webView.scrollY * 100 / webView.maxScrollY)
            scrollYSubject.onNext(webView.scrollY)
        }
        webView.webViewClient = this
        webView.verticalScrollbarPosition
        webView.settings.javaScriptEnabled = false
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        webView.settings.setSupportMultipleWindows(false)
        webView.settings.setSupportZoom(false)

        return this
    }

    fun loadUrl(url: String): MrakopediaWebViewClient {
        mWebView?.loadUrl(url)
        return this
    }

    fun zoomIn(): MrakopediaWebViewClient {
        textZoom.zoomIn()
        return this
    }

    fun zoomOut(): MrakopediaWebViewClient {
        textZoom.zoomOut()
        return this
    }

    fun resetZoom(): MrakopediaWebViewClient {
        textZoom.reset()
        return this
    }

    fun findNext(): MrakopediaWebViewClient {
        mWebView?.findNext(true)
        return this
    }

    fun findAllAsync(textToFind: String): MrakopediaWebViewClient {
        mWebView?.findAllAsync(textToFind)
        return this
    }

    fun show(): MrakopediaWebViewClient {
        mWebView?.visibility = View.VISIBLE
        return this
    }

    fun hide(): MrakopediaWebViewClient {
        mWebView?.visibility = View.INVISIBLE
        return this
    }

    override fun dispose() {
        scrollPositionSubscription.dispose()
        disposed = true
    }

    override fun isDisposed(): Boolean {
        return disposed
    }
}