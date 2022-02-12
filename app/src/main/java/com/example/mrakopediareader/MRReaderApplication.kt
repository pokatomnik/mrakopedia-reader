package com.example.mrakopediareader

import android.app.Application
import com.example.mrakopediareader.metainfo.PagesMetaInfoSource
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MRReaderApplication : Application() {
    @Inject
    lateinit var pagesMetaInfoSource: PagesMetaInfoSource

    override fun onCreate() {
        super.onCreate()
        pagesMetaInfoSource.init()
    }
}