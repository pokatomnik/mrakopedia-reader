package com.example.mrakopediareader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NavUtils
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.csvexport.CSVColumn
import com.example.mrakopediareader.csvexport.CSVSerializer
import com.example.mrakopediareader.db.Database
import com.example.mrakopediareader.db.dao.favorites.Favorite
import com.example.mrakopediareader.ui.theme.MrakopediareaderTheme
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class ExportActivityResultContract :
    ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: Context, input: String): Intent {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_CREATE_DOCUMENT
        shareIntent.type = MIME_CSV
        shareIntent.putExtra(Intent.EXTRA_TITLE, input)
        return shareIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.data
        } else null
    }

    companion object {
        private const val MIME_CSV = "text/csv"
    }
}

@AndroidEntryPoint
class FavoritesExportActivity : ComponentActivity() {
    private val exportingSubject = BehaviorSubject.createDefault(false)

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var api: API

    private val exportLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ExportActivityResultContract()) {
            it?.let { handleExport(it)}
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MrakopediareaderTheme {
                Scaffold(
                    topBar = {
                        TopAppBar {
                            NavUtils.navigateUpFromSameTask(this)
                        }
                    },
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    @Composable
    private fun TopAppBar(onBack: () -> Unit) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            title = {
                Text("Экспорт")
            }
        )
    }

    @Composable
    private fun Content() {
        val (exporting, setIsExporting) = remember {
            mutableStateOf(exportingSubject.value ?: false)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { exportLauncher.launch("mrakopedia-reader.favorites.csv") },
                enabled = !exporting
            ) {
                Text("Экспорт избранного в CSV")
            }
        }

        DisposableEffect(LocalLifecycleOwner.current) {
            val subscription = exportingSubject.subscribe(setIsExporting)
            onDispose(subscription::dispose)
        }
    }

    private fun handleExport(uri: Uri) {
        Observable.just(CSVSerializer<Favorite>(listOf(
            CSVColumn("Название") { it.title },
            CSVColumn("Ссылка") { api.getFullPagePath(it.url) }
        )))
            .doOnNext { exportingSubject.onNext(true) }
            .observeOn(Schedulers.io())
            .map { csvSerializer ->
                val favoritesDao = database.favoritesDao()
                val favorites = favoritesDao.getAll()
                csvSerializer.serialize(favorites)
            }
            .doOnNext { serialized ->
                contentResolver.openOutputStream(uri).use {
                    it?.apply {
                        write(serialized.toByteArray())
                        close()
                    }
                }
            }
            .doOnNext { exportingSubject.onNext(false) }
            .subscribe({
                runOnUiThread {
                    Toast.makeText(this@FavoritesExportActivity, "Успешно экспортировано", Toast.LENGTH_LONG).show()
                }
            }) {
                runOnUiThread {
                    Toast.makeText(this@FavoritesExportActivity, "Экспорт завершился с ошибкой", Toast.LENGTH_LONG).show()
                }
            }
    }
}