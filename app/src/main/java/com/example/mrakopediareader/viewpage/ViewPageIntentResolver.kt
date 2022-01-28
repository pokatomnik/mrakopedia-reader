package com.example.mrakopediareader.viewpage

import android.content.Intent
import android.content.res.Resources
import com.example.mrakopediareader.R

data class ViewPagePrefs(
    val pageTitle: String,
    val pagePath: String,
    val pageUrl: String
)

fun resolveIntent(
    resources: Resources,
    intent: Intent,
): ViewPagePrefs? {
    val url = intent.data;
    val encodedPath = url?.encodedPath

    val intentPageTitle = intent.getStringExtra(resources.getString(R.string.pass_page_title))
    val intentPagePath = intent.getStringExtra(resources.getString(R.string.pass_page_path))
    val intentPageUrl = intent.getStringExtra(resources.getString(R.string.pass_page_url))

    return if (Intent.ACTION_VIEW == intent.action && url != null && encodedPath != null) {
        ViewPagePrefs(url.pathSegments[2], encodedPath, intent.data.toString())
    } else if (intentPageTitle != null && intentPagePath != null && intentPageUrl != null) {
        ViewPagePrefs(intentPageTitle, intentPagePath, intentPageUrl)
    } else null
}