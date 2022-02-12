package com.example.mrakopediareader.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.ui.theme.MrakopediareaderTheme
import com.google.accompanist.flowlayout.FlowRow
import io.reactivex.rxjava3.core.Observable

@Composable
fun RandomLinks(
    api: API,
    existingRandom: List<Page>?,
    onRandomLoaded: (random: List<Page>) -> Unit,
    openPage: (page: Page) -> Unit
) {
    fun Page.empty(): Boolean {
        return this.title == "" && this.url == ""
    }

    val (randomPages, setRandomPages) = remember { mutableStateOf<List<Page>>(listOf()) }
    val observable = existingRandom?.let { Observable.just(it) } ?: remember {
        Observable.zip(
            api.getRandomPage().onErrorReturn { Page() },
            api.getRandomPage().onErrorReturn { Page() },
            api.getRandomPage().onErrorReturn { Page() }
        ) { page1, page2, page3 ->
            listOf(page1, page2, page3).filter { !it.empty() } }
    }
    MrakopediareaderTheme {
        FlowRow {
            for (randomPage in randomPages) {
                PageButton(page = randomPage, openPage = openPage)
            }
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        val subscription = observable.doOnNext(onRandomLoaded).subscribe(setRandomPages) {}
        onDispose(subscription::dispose)
    }
}

@Composable
fun PageButton(page: Page, openPage: (page: Page) -> Unit) {
    Button(
        onClick = { openPage(page) },
        modifier = Modifier.padding(8.dp, 4.dp),
    ) {
        Text(page.title.uppercase(), textAlign = TextAlign.Center)
    }
}