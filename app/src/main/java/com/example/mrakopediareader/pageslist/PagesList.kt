package com.example.mrakopediareader.pageslist

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mrakopediareader.LoadingState
import com.example.mrakopediareader.MRReaderApplication
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.viewpage.ViewPage
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.*

abstract class PagesList : AppCompatActivity() {
    private val pagesList = mutableListOf<Page>()

    private var mAdapter: RecyclerView.Adapter<PagesListViewHolder> =
        PageResultsAdapter(pagesList) { handleClick(it) }

    protected val api: API by lazy { (application as MRReaderApplication).api }

    private val recyclerView by lazy {
        findViewById<RecyclerView>(R.id.pagesListView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PagesList)
            adapter = mAdapter
        }
    }

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progressBar) }

    private val noItems by lazy { findViewById<TextView>(R.id.noItems) }

    private val loadingSubject: Subject<LoadingState> = PublishSubject.create()

    private var resultsSubscription: Disposable = Disposable.empty()

    private var loadingSubscription: Disposable = Disposable.empty()

    protected abstract fun getPages(): Observable<List<Page>>

    private fun manageVisibility(loadingState: LoadingState) {
        when (loadingState) {
            LoadingState.HAS_ERROR -> {
                recyclerView.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.INVISIBLE
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.notification_failed_get_pages_message),
                    Toast.LENGTH_LONG
                ).show()
            }
            LoadingState.LOADING -> {
                recyclerView.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
                noItems.visibility = View.INVISIBLE
            }
            LoadingState.EMPTY -> {
                recyclerView.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.VISIBLE
            }
            LoadingState.HAS_RESULTS -> {
                recyclerView.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.INVISIBLE
            }
        }
    }

    private fun handleResults(newResults: List<Page>) {
        pagesList.clear()
        pagesList.addAll(newResults)
        mAdapter.notifyDataSetChanged()
    }

    private fun handleError(ignored: Throwable) {
        loadingSubject.onNext(LoadingState.HAS_ERROR)
    }

    private fun handleClick(page: Page) {
        val intent = Intent(baseContext, ViewPage::class.java)
        intent.putExtra(resources.getString(R.string.pass_page_url), api.getFullPagePath(page.url))
        intent.putExtra(resources.getString(R.string.pass_page_title), page.title)
        intent.putExtra(resources.getString(R.string.pass_page_path), page.url)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pages_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingSubscription = loadingSubject.subscribe { manageVisibility(it) }
        loadingSubject.onNext(LoadingState.LOADING)
        resultsSubscription = getPages()
            .doOnNext {
                when (it.size) {
                    0 -> loadingSubject.onNext(LoadingState.EMPTY)
                    else -> loadingSubject.onNext(LoadingState.HAS_RESULTS)
                }
            }
            .subscribe(::handleResults, ::handleError)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingSubscription.dispose()
        resultsSubscription.dispose()
    }
}