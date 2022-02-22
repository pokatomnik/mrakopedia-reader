package com.example.mrakopediareader.pageslist

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
import com.example.mrakopediareader.LoadingState
import com.example.mrakopediareader.R
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.metainfo.PagesMetaInfoSource
import com.example.mrakopediareader.viewpage.ViewPage
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
abstract class PagesList : AppCompatActivity() {
    @Inject
    lateinit var api: API

    @Inject
    lateinit var pagesMetaInfoSource: PagesMetaInfoSource

    private val sorter by lazy { PagesSorter(pagesMetaInfoSource) }

    private val sorterSubject =
        BehaviorSubject.createDefault(PagesSorter.Companion.SortID.ALPHA_ASC)

    private val pagesList = mutableListOf<Page>()

    private val mAdapter: RecyclerView.Adapter<PagesListViewHolder> by lazy {
        PageResultsAdapter(
            pages = pagesList,
            resources = resources,
            getPageMetaInfo = { pagesMetaInfoSource.getMetaInfoByPageTitle(it) }
        ) { handleClick(it) }
    }

    private val recyclerView by lazy {
        findViewById<RecyclerView>(R.id.pagesListView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PagesList)
            adapter = mAdapter
        }
    }

    private val sortBar by lazy { findViewById<LinearLayout>(R.id.sort_bar) }

    private val buttonSortAlphabetically by lazy { findViewById<ImageButton>(R.id.sort_alphabet) }

    private val buttonSortReadingTime by lazy { findViewById<ImageButton>(R.id.sort_reading_time) }

    private val buttonSortRating by lazy { findViewById<ImageButton>(R.id.sort_rating) }

    private val buttonSortVoted by lazy { findViewById<ImageButton>(R.id.sort_voted) }

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
                sortBar.visibility = View.INVISIBLE
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
                sortBar.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
                noItems.visibility = View.INVISIBLE
            }
            LoadingState.EMPTY -> {
                recyclerView.visibility = View.INVISIBLE
                sortBar.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                noItems.visibility = View.VISIBLE
            }
            LoadingState.HAS_RESULTS -> {
                recyclerView.visibility = View.VISIBLE
                sortBar.visibility = View.VISIBLE
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
    private fun handleResults(newResults: List<Page>) {
        runOnUiThread {
            pagesList.clear()
            pagesList.addAll(newResults)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun handleError(@Suppress("UNUSED_PARAMETER") ignored: Throwable) {
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

        val pagesObservable = savedInstanceState?.getStringArrayList(KEY_PAGES)?.let {
            Observable.just(it.map(Page::parse))
        } ?: getPages()
        resultsSubscription = Observable.combineLatest(
            pagesObservable,
            sorterSubject.distinctUntilChanged()
        ) { pages, sortID -> sorter.sorted(sortID, pages) }
            .doOnNext {
                runOnUiThread {
                    when (it.size) {
                        0 -> loadingSubject.onNext(LoadingState.EMPTY)
                        else -> loadingSubject.onNext(LoadingState.HAS_RESULTS)
                    }
                }
            }
            .subscribeOn(Schedulers.single())
            .subscribe(::handleResults, ::handleError)
        savedInstanceState?.getSerializable(KEY_SORT_ID)?.let {
            (it as? PagesSorter.Companion.SortID)?.let { savedSortID ->
                sorterSubject.onNext(savedSortID)
            }
        }
        with(sorterSubject) {
            buttonSortAlphabetically.setOnClickListener { onNext(PagesSorter.nextAlpha(sorterSubject.value)) }
            buttonSortReadingTime.setOnClickListener {
                onNext(
                    PagesSorter.nextReadingTime(
                        sorterSubject.value
                    )
                )
            }
            buttonSortRating.setOnClickListener { onNext(PagesSorter.nextRating(sorterSubject.value)) }
            buttonSortVoted.setOnClickListener { onNext(PagesSorter.nextVoted(sorterSubject.value)) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun MutableList<Page>.serialize(): ArrayList<String> {
        return ArrayList(map(Page::serialize))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(KEY_PAGES, pagesList.serialize())
        outState.putSerializable(KEY_SORT_ID, sorterSubject.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingSubscription.dispose()
        resultsSubscription.dispose()
    }

    companion object {
        private const val KEY_PAGES = "KEY_PAGES"
        private const val KEY_SORT_ID = "KEY_SORT_ID"
    }
}