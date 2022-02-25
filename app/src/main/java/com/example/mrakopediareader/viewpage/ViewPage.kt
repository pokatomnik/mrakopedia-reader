package com.example.mrakopediareader.viewpage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.example.mrakopediareader.ExternalLinks
import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.categorieslist.CategoriesByPage
import com.example.mrakopediareader.db.Database
import com.example.mrakopediareader.db.dao.favorites.Favorite
import com.example.mrakopediareader.db.dao.recent.Recent
import com.example.mrakopediareader.db.dao.scrollposition.ScrollPosition
import com.example.mrakopediareader.linkshare.shareLink
import com.example.mrakopediareader.pageslist.RelatedList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private fun Menu.setScrollTopChecked(checked: Boolean) {
    findItem(R.id.display_scroll_top).isChecked = checked
}

private fun Menu.setFavoriteItemExists(exists: Boolean) {
    val menuItem = findItem(R.id.favorites)
    if (exists) {
        menuItem.setTitle(R.string.ui_add_to_to_favorites)
        menuItem.setIcon(R.drawable.ic_fav_unselected)
    } else {
        menuItem.setTitle(R.string.ui_remove_from_favorites)
        menuItem.setIcon(R.drawable.ic_fav_selected)
    }
}

private fun Menu.setDarkModeChecked(darkMode: Boolean) {
    val menuItem = findItem(R.id.toggle_dark_mode)
    menuItem.isChecked = darkMode
}

@AndroidEntryPoint
class ViewPage : AppCompatActivity() {
    @Inject
    lateinit var api: API

    @Inject
    lateinit var database: Database

    private val scrollTopFAB by lazy { findViewById<FloatingActionButton>(R.id.scrollTopFAB) }

    private val preferences by lazy { Preferences(getPreferences(Context.MODE_PRIVATE)) }

    private var scrollTopVisibleDisposable = Disposable.empty()

    private var scrollPositionRestorationDisposable = Disposable.empty()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    private val mFavoritesStore: FavoritesStore by lazy { FavoritesStore(database) }

    private val mViewPagePrefs: ViewPagePrefs? by lazy { resolveIntent(resources, intent) }

    private val searchView: SearchView by lazy { findViewById(R.id.searchView) }

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.pageLoadingProgressBar) }

    private val defaultActionbarTitle by lazy {
        supportActionBar?.title
    }

    private val scrollYSubject = PublishSubject.create<Int>()

    private val scrollYPercentSubject = BehaviorSubject.createDefault(0)

    /**
     * Prevents scroll reset when the loading is in progress
     * Atomic, because multiple threads are writing to It
     */
    private val scrollListenersEnabled = AtomicBoolean(false)

    private val webView by lazy {
        findViewById<MRWebView>(R.id.webView).apply {
            webViewClient = MrakopediaWebViewClient(
                onStartLoading = {
                    scrollListenersEnabled.set(false)
                    hide()
                    scrollTopFAB.isEnabled = false
                    progressBar.visibility = View.VISIBLE
                },
                onFinishLoading = {
                    scrollListenersEnabled.set(true)
                    show()
                    scrollTopFAB.isEnabled = true
                    progressBar.visibility = View.INVISIBLE
                    scrollPositionRestorationDisposable = restoreScrollPosition()
                        .subscribe { (_, position) -> scrollY = position }
                }
            )
        }
    }

    private val scrollYSubscription = scrollYSubject
        .filter { scrollListenersEnabled.get() }
        .observeOn(Schedulers.io())
        .debounce(2000, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .subscribe { scrollY ->
            mViewPagePrefs?.pageTitle?.let { pageTitle ->
                database.scrollPositionsDao().setPosition(ScrollPosition(pageTitle, scrollY))
            }
        }

    private val scrollPercentSubscription = scrollYPercentSubject
        .filter { scrollListenersEnabled.get() }
        .onErrorReturn { 0 }
        .debounce(200, TimeUnit.MILLISECONDS)
        .subscribe {
            runOnUiThread {
                supportActionBar?.apply {
                    title = "$defaultActionbarTitle ${it}%"
                }
            }
        }

    private var mMenu: Menu? = null

    private fun restoreScrollPosition(): Observable<ScrollPosition> {
        return mViewPagePrefs?.let { viewPagePrefs ->
            Observable
                .just(database.scrollPositionsDao())
                .observeOn(Schedulers.io())
                .map { scrollPositionDAO ->
                    val position = scrollPositionDAO.getPosition(viewPagePrefs.pageTitle)
                    position ?: ScrollPosition(viewPagePrefs.pageTitle, 0)
                }
                .onErrorReturn {
                    ScrollPosition(viewPagePrefs.pageTitle, 0)
                }
        } ?: Observable.never()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_page)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                webView.findNext(true)
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                webView.findAllAsync(s)
                return true
            }
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mViewPagePrefs?.let { viewPagePrefs ->
            webView.setOnScrollChangeListener { _, _, _, _, _ ->
                scrollYPercentSubject.onNext(if (webView.maxScrollY > 0) webView.scrollY * 100 / webView.maxScrollY else 0)
                scrollYSubject.onNext(webView.scrollY)
            }
            webView.loadUrl(viewPagePrefs.pageUrl, preferences.darkModeEnabled)
        }

        scrollTopVisibleDisposable = preferences.observeScrollTopVisible().subscribe {
            scrollTopFAB.visibility = if (it) View.VISIBLE else View.INVISIBLE
            mMenu?.setScrollTopChecked(preferences.scrollTopVisible)
        }

        scrollTopFAB.setOnClickListener { webView.scrollTop() }

        mViewPagePrefs?.let { viewPagePrefs ->
            Observable
                .just(viewPagePrefs)
                .observeOn(Schedulers.io())
                .map {
                    Recent(it.pageTitle, it.pageUrl, Calendar.getInstance().time.time)
                }
                .subscribe {
                    database.recentDao().upsert(it)
                }
        }
    }

    private fun handleShareIntent(intent: Intent?) {
        intent?.let { startActivity(it) }
    }

    private fun toggleFavorite() {
        mViewPagePrefs?.let {
            coroutineScope.launch {
                val exists = mFavoritesStore.has(it.pageTitle)
                if (exists) {
                    mFavoritesStore.remove(it.pageTitle)
                } else {
                    mFavoritesStore.set(Favorite(title = it.pageTitle, url = it.pagePath))
                }
                runOnUiThread {
                    mMenu?.setFavoriteItemExists(exists)
                }
            }
        }
    }

    private fun openRelated() {
        mViewPagePrefs?.let {
            val intent = Intent(baseContext, RelatedList::class.java)
            intent.putExtra(resources.getString(R.string.pass_page_title), it.pageTitle)
            startActivity(intent)
        }
    }

    private fun openCategories() {
        mViewPagePrefs?.let {
            val intent = Intent(baseContext, CategoriesByPage::class.java)
            intent.putExtra(resources.getString(R.string.pass_page_title), it.pageTitle)
            startActivity(intent)
        }
    }

    private fun toggleSearchBar() {
        mMenu?.apply {
            val searchMenuItem = findItem(R.id.search_in_page)
            val checked = searchMenuItem.isChecked
            if (checked) {
                searchMenuItem.isChecked = false
                searchView.setQuery("", false)
                searchView.visibility = View.GONE
            } else {
                searchMenuItem.isChecked = true
                searchView.visibility = View.VISIBLE
                searchView.requestFocus()
            }
        }
    }

    private fun openMrakopedia() {
        mViewPagePrefs?.let { prefs ->
            api.getWebsiteUrlForPage(prefs.pageTitle)
                .subscribe({ startActivity(ExternalLinks(resources).openWebsiteUrl(it.uri)) })
                { Toast.makeText(this, "Ошибка открытия страницы", Toast.LENGTH_LONG).show() }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                onBackPressed()
                true
            }
            R.id.favorites -> {
                toggleFavorite()
                true
            }
            R.id.zoom_in -> {
                webView.pageZoomIn()
                true
            }
            R.id.zoom_out -> {
                webView.pageZoomOut()
                true
            }
            R.id.reset_zoom -> {
                webView.resetZoom()
                true
            }
            R.id.share -> {
                mViewPagePrefs?.let {
                    handleShareIntent(shareLink(it.pageTitle, it.pageUrl))
                }
                true
            }
            R.id.related -> {
                openRelated()
                true
            }
            R.id.categories -> {
                openCategories()
                true
            }
            R.id.open_mrakopedia -> {
                openMrakopedia()
                true
            }
            R.id.search_in_page -> {
                toggleSearchBar()
                super.onOptionsItemSelected(item)
            }
            R.id.display_scroll_top -> {
                preferences.toggleScrollTopVisible()
                super.onOptionsItemSelected(item)
            }
            R.id.toggle_dark_mode -> {
                preferences.toggleDarkModeEnabled()
                mViewPagePrefs?.let { webView.loadUrl(it.pageUrl, preferences.darkModeEnabled) }
                mMenu?.setDarkModeChecked(preferences.darkModeEnabled)
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu
        menuInflater.inflate(R.menu.view_page_menu, menu)
        menu.setScrollTopChecked(preferences.scrollTopVisible)
        menu.setDarkModeChecked(preferences.darkModeEnabled)
        val menuItem = menu.findItem(R.id.favorites)

        mViewPagePrefs?.let {
            coroutineScope.launch {
                val exists = mFavoritesStore.has(it.pageTitle)
                runOnUiThread {
                    if (exists) {
                        menuItem.setTitle(R.string.ui_remove_from_favorites)
                        menuItem.setIcon(R.drawable.ic_fav_selected)
                    } else {
                        menuItem.setTitle(R.string.ui_add_to_to_favorites)
                        menuItem.setIcon(R.drawable.ic_fav_unselected)
                    }
                }
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollTopVisibleDisposable.dispose()

        scrollPositionRestorationDisposable.dispose()
        scrollYSubscription.dispose()
        scrollPercentSubscription.dispose()
    }
}