package com.example.mrakopediareader.metainfo

import com.example.mrakopediareader.api.API
import com.example.mrakopediareader.api.APIModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MetaInfo {
    @Singleton
    @Provides
    fun providePagesMetaInfoSource(@APIModule.Companion.DependencyAPI api: API): PagesMetaInfoSource {
        return PagesMetaInfoSource(api)
    }
}