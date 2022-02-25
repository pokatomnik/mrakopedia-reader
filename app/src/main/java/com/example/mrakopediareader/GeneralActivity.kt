package com.example.mrakopediareader

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.categorieslist.AllCategories
import com.example.mrakopediareader.metainfo.PagesMetaInfoSource
import com.example.mrakopediareader.pageslist.FavoritesList
import com.example.mrakopediareader.pageslist.HOTMList
import com.example.mrakopediareader.pageslist.RecentList
import com.example.mrakopediareader.pageslist.SearchResults
import com.example.mrakopediareader.ui.components.RandomLinks
import com.example.mrakopediareader.viewpage.ViewPage
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@AndroidEntryPoint
class GeneralActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var randomLoaded: List<Page>? = null

    private val externalLinks by lazy { ExternalLinks(resources) }

    @Inject
    lateinit var api: API

    @Inject
    lateinit var pagesMetaInfoSource: PagesMetaInfoSource

    private val inputSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")

    private val buttonClearSearch by lazy { findViewById<ImageButton>(R.id.clear_search) }

    private val busynessSubject = BehaviorSubject.createDefault(false)

    private val searchButton by lazy { findViewById<Button>(R.id.searchButton) }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }

    private val drawer by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }

    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }

    private val editText by lazy {
        findViewById<AutoCompleteTextView>(R.id.searchText).apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch()
                    true
                } else false
            }
        }
    }

    private lateinit var allPageTitlesSuggestionsSubscription: Disposable

    private val generalLinearLayout by lazy { findViewById<LinearLayout>(R.id.generalLinearLayout) }

    private val generalProgressbar by lazy { findViewById<ProgressBar>(R.id.generalProgressBar) }

    private var randomPageSubscription = Disposable.empty()

    private var busynessSubscription = Disposable.empty()

    private var searchStringChangeSubscription = Disposable.empty()

    private var clearSearchVisibilitySubscription = Disposable.empty()

    private fun handleRandom(random: List<Page>) {
        randomLoaded = random
    }

    private fun performSearch() {
        val intent = Intent(baseContext, SearchResults::class.java)
        intent.putExtra(
            resources.getString(R.string.pass_search_string_intent_key),
            inputSubject.value
        )
        startActivity(intent)
    }

    fun handleClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        performSearch()
    }

    fun handleClearClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        editText.text.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.drawer_navigation_drawer_open,
            R.string.drawer_navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        allPageTitlesSuggestionsSubscription = pagesMetaInfoSource
            .observePageTitles()
            .subscribe {
                val adapter = ArrayAdapter(
                    this@GeneralActivity,
                    android.R.layout.simple_expandable_list_item_1,
                    it.toList()
                )
                editText.setAdapter(adapter)
            }
        busynessSubscription = busynessSubject.subscribe(::manageVisibility)
        searchStringChangeSubscription = inputSubject
            .distinctUntilChanged()
            .subscribe(::handleSearchStringChange)

        inputSubject.map { it != "" }.subscribe {
            buttonClearSearch.visibility = if (it) View.VISIBLE else View.GONE
        }

        editText.addTextChangedListener(SearchTextWatcher(inputSubject::onNext))

        val composeView = ComposeView(this).apply {
            val existingRandomLoaded = savedInstanceState?.let {
                val listOfSerialized = it.getStringArrayList(KEY_RANDOM_LOADED)
                listOfSerialized?.let { serialized ->
                    serialized.map { pageData -> Page.parse(pageData) }
                }
            }
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setContent {
                // We then render a simple Text component from Compose.
                RandomLinks(api, existingRandomLoaded, { handleRandom(it) }) { openPage(it) }
            }
        }

        generalLinearLayout.addView(composeView)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchStringChangeSubscription.dispose()
        randomPageSubscription.dispose()
        busynessSubscription.dispose()
        allPageTitlesSuggestionsSubscription.dispose()
        clearSearchVisibilitySubscription.dispose()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun manageVisibility(busy: Boolean) {
        if (busy) {
            generalLinearLayout.visibility = View.INVISIBLE
            generalProgressbar.visibility = View.VISIBLE
        } else {
            generalLinearLayout.visibility = View.VISIBLE
            generalProgressbar.visibility = View.INVISIBLE
        }
    }

    private fun handleSearchStringChange(newSearchString: String) {
        searchButton.isEnabled = newSearchString.trim().isNotEmpty()
    }

    private fun openPage(page: Page) {
        val intent = Intent(baseContext, ViewPage::class.java)
        intent.putExtra(resources.getString(R.string.pass_page_url), api.getFullPagePath(page.url))
        intent.putExtra(resources.getString(R.string.pass_page_title), page.title)
        intent.putExtra(resources.getString(R.string.pass_page_path), page.url)
        startActivity(intent)
    }

    private fun handleGetRandomPageFailed(@Suppress("UNUSED_PARAMETER") throwable: Throwable) {
        val text = resources.getString(R.string.notification_failed_get_random_page_message)
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    private fun openRandomPage() {
        busynessSubject.onNext(true)
        randomPageSubscription = this.api
            .getRandomPage()
            .doFinally { busynessSubject.onNext(false) }
            .subscribe(::openPage, ::handleGetRandomPageFailed)
    }

    private fun openCategories() {
        val intent = Intent(baseContext, AllCategories::class.java)
        startActivity(intent)
    }

    private fun openFavorites() {
        val intent = Intent(baseContext, FavoritesList::class.java)
        startActivity(intent)
    }

    private fun openRecent() {
        val intent = Intent(baseContext, RecentList::class.java)
        startActivity(intent)
    }

    private fun openHotmPages() {
        val intent = Intent(baseContext, HOTMList::class.java)
        startActivity(intent)
    }

    private fun openExport() {
        val intent = Intent(baseContext, FavoritesExportActivity::class.java)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_random_page -> openRandomPage()
            R.id.nav_categories -> openCategories()
            R.id.nav_favorites -> openFavorites()
            R.id.nav_recent -> openRecent()
            R.id.nav_hotm -> openHotmPages()
            R.id.nav_export -> openExport()
            R.id.nav_bug_or_enhancement -> startActivity(externalLinks.newIssue())
            R.id.nav_telegram -> startActivity(externalLinks.openTelegram())
            R.id.nav_email -> startActivity(externalLinks.openMailClient())
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        randomLoaded?.let {
            val randomSerialized = ArrayList(it.map(Page::serialize))
            outState.putStringArrayList(KEY_RANDOM_LOADED, randomSerialized)
        }
    }

    companion object {
        const val KEY_RANDOM_LOADED = "KEY_RANDOM_LOADED"
    }
}