package com.example.mrakopediareader

import android.app.Application
import com.example.mrakopediareader.api.API

class MRReaderApplication : Application() {
    val api: API by lazy {
        API(applicationContext)
    }
}