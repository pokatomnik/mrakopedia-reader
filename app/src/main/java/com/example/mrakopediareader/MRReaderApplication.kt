package com.example.mrakopediareader

import android.app.Application
import androidx.room.Room
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.dto.PageMetaInfo
import com.example.mrakopediareader.db.Database

class MRReaderApplication : Application() {
    private var pagesMetaInfoIndex: Map<String, PageMetaInfo>? = null

    val api: API by lazy {
        API(applicationContext)
    }

    val database by lazy {
        Room.databaseBuilder(applicationContext, Database::class.java, "mrreader.db").build()
    }

    override fun onCreate() {
        super.onCreate()
        api.getPagesMetaInfoIndex().subscribe {
            pagesMetaInfoIndex = it
        }
    }

    fun getMetaInfoByPageTitle(title: String): PageMetaInfo? {
        return pagesMetaInfoIndex?.get(title)
    }
}