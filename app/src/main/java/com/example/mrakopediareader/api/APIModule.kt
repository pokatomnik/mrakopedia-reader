package com.example.mrakopediareader.api

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class APIModule {
    private var api: API? = null

    private fun getAPIAndMemoize(applicationContext: Context): API {
        val expectedAPI: API = api ?: API(applicationContext)
        api = expectedAPI
        return expectedAPI
    }

    @Singleton
    @Provides
    fun provideAPI(@ApplicationContext applicationContext: Context): API {
        return getAPIAndMemoize(applicationContext)
    }

    @DependencyAPI
    @Provides
    @Singleton
    fun provideAPIAsDependency(@ApplicationContext applicationContext: Context): API {
        return getAPIAndMemoize(applicationContext)
    }

    companion object {
        @Qualifier
        @Retention(AnnotationRetention.RUNTIME)
        annotation class DependencyAPI
    }
}