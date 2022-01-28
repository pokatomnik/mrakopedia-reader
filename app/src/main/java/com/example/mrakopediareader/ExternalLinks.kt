package com.example.mrakopediareader

import android.content.Intent
import android.content.res.Resources
import android.net.Uri

class ExternalLinks(resources: Resources) {
    private val issuesUrl: String = resources.getString(R.string.drawer_report_issue)

    private val telegramUrl: String = resources.getString(R.string.drawer_open_telegram)

    private val mailUrl: String = resources.getString(R.string.drawer_open_email)

    fun newIssue(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(issuesUrl))
    }

    fun openTelegram(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
    }

    fun openMailClient(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(mailUrl))
    }

    fun openWebsiteUrl(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW, uri)
    }

}