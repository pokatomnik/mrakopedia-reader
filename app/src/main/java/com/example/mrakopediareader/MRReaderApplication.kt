package com.example.mrakopediareader

import android.app.Application
import androidx.room.Room
import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.db.dao.Database

class MRReaderApplication : Application() {
    val api: API by lazy {
        API(applicationContext)
    }

    val database by lazy {
        Room.databaseBuilder(applicationContext, Database::class.java, "mrreader.db").build()
    }
}