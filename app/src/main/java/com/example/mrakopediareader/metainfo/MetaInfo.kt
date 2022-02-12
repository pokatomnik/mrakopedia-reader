package com.example.mrakopediareader.metainfo

import com.example.mrakopediareader.api.API
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
    fun providePagesMetaInfoSource(api: API): PagesMetaInfoSource {
        return PagesMetaInfoSource(api)
    }
}