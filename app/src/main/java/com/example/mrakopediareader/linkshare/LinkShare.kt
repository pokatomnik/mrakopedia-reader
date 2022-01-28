package com.example.mrakopediareader.linkshare

import android.content.Intent

fun shareLink(title: String, url: String): Intent? {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)
    return try {
        Intent.createChooser(shareIntent, "Куда отправить")
    } catch (e: Error) {
        null
    }
}