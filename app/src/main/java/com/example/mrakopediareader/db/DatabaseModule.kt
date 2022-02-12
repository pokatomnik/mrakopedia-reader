package com.example.mrakopediareader.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): Database {
        return Room
            .databaseBuilder(applicationContext, Database::class.java, "mrreader.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}