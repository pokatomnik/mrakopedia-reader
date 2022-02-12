package com.example.mrakopediareader.api

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class APIModule {
    private fun getAPIAndMemoize(applicationContext: Context): API {
        return API(applicationContext)
    }

    @Singleton
    @Provides
    fun provideAPI(@ApplicationContext applicationContext: Context): API {
        return getAPIAndMemoize(applicationContext)
    }
}