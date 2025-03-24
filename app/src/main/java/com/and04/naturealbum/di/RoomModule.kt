package com.and04.naturealbum.di

import android.content.Context
import com.and04.naturealbum.data.datasource.local.LocalAlbumDataSource
import com.and04.naturealbum.data.datasource.local.LabelDataSource
import com.and04.naturealbum.data.datasource.local.PhotoDetailDataSource
import com.and04.naturealbum.data.localdata.room.AlbumDao
import com.and04.naturealbum.data.localdata.room.AppDatabase
import com.and04.naturealbum.data.localdata.room.LabelDao
import com.and04.naturealbum.data.localdata.room.PhotoDetailDao
import com.and04.naturealbum.data.repository.local.LabelRepository
import com.and04.naturealbum.data.repository.local.LocalAlbumRepository
import com.and04.naturealbum.data.repository.local.PhotoDetailRepository
import com.and04.naturealbum.data.repository.local.SyncRepository
import com.and04.naturealbum.data.repository.local.impl.LabelRepositoryImpl
import com.and04.naturealbum.data.repository.local.impl.LocalAlbumRepositoryImpl
import com.and04.naturealbum.data.repository.local.impl.PhotoDetailRepositoryImpl
import com.and04.naturealbum.data.repository.local.impl.SyncRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun providerAppDatabase(
        @ApplicationContext context: Context
    ) = AppDatabase.getDatabase(context)

    @Singleton
    @Provides
    fun providerLabelDao(
        database: AppDatabase
    ) = database.labelDao()

    @Singleton
    @Provides
    fun providerAlbumDao(
        database: AppDatabase
    ) = database.albumDao()

    @Singleton
    @Provides
    fun providerPhotoDetailDao(
        database: AppDatabase
    ) = database.photoDetailDao()

    @Singleton
    @Provides
    fun providerLabelDataSource(
        labelDao: LabelDao
    ) = LabelDataSource(labelDao)

    @Singleton
    @Provides
    fun providerLabelRepo(
        labelDataSource: LabelDataSource
    ): LabelRepository = LabelRepositoryImpl(labelDataSource)

    @Singleton
    @Provides
    fun providerPhotoDetailDataSource(
        photoDetailDao: PhotoDetailDao
    ) = PhotoDetailDataSource(photoDetailDao)

    @Singleton
    @Provides
    fun providerPhotoDetailRepository(
        localAlbumDataSource: LocalAlbumDataSource,
        photoDetailDataSource: PhotoDetailDataSource,
    ): PhotoDetailRepository = PhotoDetailRepositoryImpl(localAlbumDataSource, photoDetailDataSource)

    @Singleton
    @Provides
    fun providerSyncRepo(
        labelDataSource: LabelDataSource,
        localAlbumDataSource: LocalAlbumDataSource,
        photoDetailDataSource: PhotoDetailDataSource
    ): SyncRepository = SyncRepositoryImpl(labelDataSource, localAlbumDataSource, photoDetailDataSource)

    @Singleton
    @Provides
    fun providerLocalAlbumDataSource(
        albumDao: AlbumDao,
    ) = LocalAlbumDataSource(albumDao)

    @Singleton
    @Provides
    fun providerLocalAlbumRepo(
        localAlbumDataSource: LocalAlbumDataSource,
    ): LocalAlbumRepository = LocalAlbumRepositoryImpl(localAlbumDataSource)

}
