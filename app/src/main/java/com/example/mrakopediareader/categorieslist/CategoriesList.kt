package com.example.mrakopediareader.categorieslist

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.*
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.Category
import com.example.mrakopediareader.pageslist.PagesByCategory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

abstract class CategoriesList : AppCompatActivity() {
    protected val api: API by lazy { (application as MRReaderApplication).api }

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

    private val categoryFilter = Filterable<String, Category>("", { search, category ->
        search == "" || category.title.lowercase().contains(search.lowercase())
    })

    private val loadingSubject  = PublishSubject.create<LoadingState>()

    private var resultSubscription: Disposable = Disposable.empty()

    private var loadingSubscription: Disposable = Disposable.empty()

    private var categoryFilterSubscription: Disposable = Disposable.empty()

    protected abstract fun getCategories(): Observable<List<Category>>

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

        loadingSubject.onNext(LoadingState.LOADING)

        resultSubscription = getCategories()
            .doOnNext {
                when (it.size) {
                    0 -> loadingSubject.onNext(LoadingState.EMPTY)
                    else -> loadingSubject.onNext(LoadingState.HAS_RESULTS)
                }
            }
            .subscribe(this.categoryFilter::updateSource) { loadingSubject.onNext(LoadingState.HAS_ERROR) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> { onBackPressed(); NavUtils.navigateUpFromSameTask(this); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingSubscription.dispose()
        resultSubscription.dispose()
        categoryFilterSubscription.dispose()
    }
}