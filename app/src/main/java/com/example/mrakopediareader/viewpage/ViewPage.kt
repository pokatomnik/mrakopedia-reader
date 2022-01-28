package com.example.mrakopediareader.viewpage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.example.mrakopediareader.ExternalLinks
import com.example.mrakopediareader.FavoritesStore
import com.example.mrakopediareader.MRReaderApplication
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.categorieslist.CategoriesByPage
import com.example.mrakopediareader.linkshare.shareLink
import com.example.mrakopediareader.pageslist.RelatedList

class ViewPage : AppCompatActivity() {
    private val api: API by lazy { (application as MRReaderApplication).api}

    private val mFavoritesStore: FavoritesStore by lazy { FavoritesStore(baseContext) }

    private val mViewPagePrefs: ViewPagePrefs? by lazy {
        resolveIntent(resources, intent)
    }

    private val webViewClient: MrakopediaWebViewClient by lazy {
        MrakopediaWebViewClient {
            val progressBar = findViewById<ProgressBar>(R.id.pageLoadingProgressBar)
            if (it) {
                webViewClient.hide()
                progressBar.visibility = View.VISIBLE
            } else {
                webViewClient.show()
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private lateinit var mMenu: Menu

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
    }

    private fun handleShareIntent(intent: Intent?) {
        intent?.let { startActivity(it) }
    }

    private fun toggleFavorite() {
        mViewPagePrefs?.let {
            val menuItem = mMenu.findItem(R.id.favorites)
            if (mFavoritesStore.has(it.pageTitle)) {
                mFavoritesStore.remove(it.pageTitle)
                menuItem.setTitle(R.string.ui_add_to_to_favorites)
                menuItem.setIcon(R.drawable.ic_fav_unselected)
            } else {
                mFavoritesStore[it.pageTitle] = it.pagePath
                menuItem.setTitle(R.string.ui_remove_from_favorites)
                menuItem.setIcon(R.drawable.ic_fav_selected)
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
        val searchMenuItem = mMenu.findItem(R.id.search_in_page)
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

    private fun openMrakopedia() {
        mViewPagePrefs?.let { prefs ->
            api.getWebsiteUrlForPage(prefs.pageTitle)
                .subscribe { startActivity(ExternalLinks(resources).openWebsiteUrl(it.uri)) }
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu

        menuInflater.inflate(R.menu.view_page_menu, menu)
        val menuItem = menu.findItem(R.id.favorites)

        mViewPagePrefs?.let {
            if (mFavoritesStore.has(it.pageTitle)) {
                menuItem.setTitle(R.string.ui_remove_from_favorites)
                menuItem.setIcon(R.drawable.ic_fav_selected)
            } else {
                menuItem.setTitle(R.string.ui_add_to_to_favorites)
                menuItem.setIcon(R.drawable.ic_fav_unselected)
            }
        }

        return true
    }
}