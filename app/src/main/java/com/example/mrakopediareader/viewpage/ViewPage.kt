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
import com.example.mrakopediareader.linkshare.shareLink
import com.example.mrakopediareader.pageslist.RelatedList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
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

@AndroidEntryPoint
class ViewPage : AppCompatActivity() {
    @Inject
    lateinit var api: API

    @Inject
    lateinit var database: Database

    private val scrollTopFAB by lazy { findViewById<FloatingActionButton>(R.id.scrollTopFAB) }

    private val preferences by lazy { Preferences(getPreferences(Context.MODE_PRIVATE)) }

    private var scrollTopVisibleDisposable = Disposable.empty()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    private val defaultActionbarTitle by lazy {
        supportActionBar?.title
    }

    private val scrollYSubject = BehaviorSubject.createDefault(0)

    private val scrollSubscription = scrollYSubject
        .debounce(200, TimeUnit.MILLISECONDS)
        .subscribe ({
            runOnUiThread {
                supportActionBar?.apply {
                    title = "$defaultActionbarTitle ${it}%"
                }
            }
        }) {}

    private val mFavoritesStore: FavoritesStore by lazy {
        FavoritesStore(database)
    }

    private val mViewPagePrefs: ViewPagePrefs? by lazy {
        resolveIntent(resources, intent)
    }

    private val webViewClient: MrakopediaWebViewClient by lazy {
        MrakopediaWebViewClient(
            mViewPagePrefs?.pageTitle ?: "",
            database.scrollPositionsDao(),
            scrollYSubject
        ) {
            val progressBar = findViewById<ProgressBar>(R.id.pageLoadingProgressBar)
            if (it) {
                webViewClient.hide()
                scrollTopFAB.isEnabled = false
                progressBar.visibility = View.VISIBLE
            } else {
                webViewClient.show()
                scrollTopFAB.isEnabled = true
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private var mMenu: Menu? = null

    private val searchView: SearchView by lazy { findViewById(R.id.searchView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_page)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                webViewClient.findNext()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                webViewClient.findAllAsync(s)
                return true
            }
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mViewPagePrefs?.let {
            webViewClient.attach(findViewById(R.id.webView)).loadUrl(it.pageUrl)
        }

        scrollTopVisibleDisposable = preferences.observeScrollTopVisible().subscribe {
            scrollTopFAB.visibility = if (it) View.VISIBLE else View.INVISIBLE
            mMenu?.setScrollTopChecked(preferences.scrollTopVisible)
        }

        scrollTopFAB.setOnClickListener { webViewClient.scrollTop() }
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
                .subscribe ({ startActivity(ExternalLinks(resources).openWebsiteUrl(it.uri)) })
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
                webViewClient.zoomIn()
                true
            }
            R.id.zoom_out -> {
                webViewClient.zoomOut()
                true
            }
            R.id.reset_zoom -> {
                webViewClient.resetZoom()
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu
        menuInflater.inflate(R.menu.view_page_menu, menu)
        menu.setScrollTopChecked(preferences.scrollTopVisible)
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
        scrollSubscription.dispose()
        webViewClient.dispose()
        scrollTopVisibleDisposable.dispose()
    }
}