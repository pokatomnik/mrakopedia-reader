package com.example.mrakopediareader.categorieslist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.Filterable
import com.example.mrakopediareader.LoadingState
import com.example.mrakopediareader.R
import com.example.mrakopediareader.SearchTextWatcher
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.Category
import com.example.mrakopediareader.pageslist.PagesByCategory
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
abstract class CategoriesList : AppCompatActivity() {
    @Inject
    lateinit var api: API

    private val filteredCategories = mutableListOf<Category>()

    private val mAdapter: RecyclerView.Adapter<CategoriesListViewHolder> =
        CategoriesAdapter(filteredCategories) { handleClick(it) }

    private val recyclerView by lazy {
        findViewById<RecyclerView>(R.id.categoriesView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CategoriesList)
            adapter = mAdapter
        }
    }

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progressBar) }

    private val noItems by lazy { findViewById<TextView>(R.id.noItems) }

    private val searchBy by lazy { findViewById<EditText>(R.id.categoriesSearchBy) }

    private val clearTextButton by lazy { findViewById<ImageButton>(R.id.clear_search) }

    private val categoryFilter = Filterable<String, Category>("") { search, category ->
        search == "" || category.title.lowercase().contains(search.lowercase())
    }

    private val loadingSubject  = PublishSubject.create<LoadingState>()

    private var resultSubscription: Disposable = Disposable.empty()

    private var loadingSubscription: Disposable = Disposable.empty()

    private var categoryFilterSubscription: Disposable = Disposable.empty()

    private var clearSearchVisibilitySubscription: Disposable = Disposable.empty()

    protected abstract fun getCategories(): Observable<List<Category>>

    fun handleClearClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        searchBy.text.clear()
    }

    private fun manageVisibility(loadingState: LoadingState) {
        when (loadingState) {
            LoadingState.HAS_ERROR -> {
                recyclerView.visibility = View.INVISIBLE
                searchBy.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.INVISIBLE
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.notification_failed_categories),
                    Toast.LENGTH_LONG
                ).show()
            }
            LoadingState.LOADING -> {
                recyclerView.visibility = View.INVISIBLE
                searchBy.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
                noItems.visibility = View.INVISIBLE
            }
            LoadingState.EMPTY -> {
                searchBy.visibility = View.INVISIBLE
                recyclerView.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.VISIBLE
            }
            LoadingState.HAS_RESULTS -> {
                searchBy.visibility = View.VISIBLE
                recyclerView.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * The dataset is being completely rebuilt here,
     * so the warning must be suppressed
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateFilteredResults(newCategories: Collection<Category>) {
        filteredCategories.clear()
        filteredCategories.addAll(newCategories)
        mAdapter.notifyDataSetChanged()
    }

    private fun handleClick(category: Category) {
        val intent = Intent(baseContext, PagesByCategory::class.java)
        intent.putExtra(resources.getString(R.string.pass_category_name), category.title)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchBy.addTextChangedListener(SearchTextWatcher { categoryFilter.updateSearch(it) })

        loadingSubscription = loadingSubject.subscribe { manageVisibility(it) }

        categoryFilterSubscription = categoryFilter.searchResultSubj
            .subscribe { updateFilteredResults(it) }

        clearSearchVisibilitySubscription = categoryFilter.observeSearch().map {
            it != ""
        }.subscribe {
            clearTextButton.visibility = if (it) View.VISIBLE else View.GONE
        }

        loadingSubject.onNext(LoadingState.LOADING)

        val categoriesObservable = savedInstanceState?.getStringArrayList(KEY_CATEGORIES)?.let {
            Observable.just(it.map(Category::parse))
        } ?: getCategories()

        resultSubscription = categoriesObservable
            .doOnNext {
                when (it.size) {
                    0 -> loadingSubject.onNext(LoadingState.EMPTY)
                    else -> loadingSubject.onNext(LoadingState.HAS_RESULTS)
                }
            }
            .subscribe(this.categoryFilter::updateSource) { loadingSubject.onNext(LoadingState.HAS_ERROR) }

        savedInstanceState?.getString(KEY_SEARCH_VALUE)?.let(categoryFilter::updateSearch)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> { onBackPressed(); NavUtils.navigateUpFromSameTask(this); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun Collection<Category>.serialize(): ArrayList<String> {
        return ArrayList(map(Category::serialize))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(KEY_CATEGORIES, categoryFilter.items?.serialize())
        outState.putString(KEY_SEARCH_VALUE, categoryFilter.searchValue)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingSubscription.dispose()
        resultSubscription.dispose()
        categoryFilterSubscription.dispose()
        clearSearchVisibilitySubscription.dispose()
    }

    companion object {
        private const val KEY_CATEGORIES = "KEY_CATEGORIES"
        private const val KEY_SEARCH_VALUE = "KEY_SEARCH_VALUE"
    }
}