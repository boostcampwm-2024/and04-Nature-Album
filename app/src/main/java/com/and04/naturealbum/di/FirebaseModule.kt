package com.and04.naturealbum.di

import com.and04.naturealbum.data.datasource.remote.RemoteAlbumDataSource
import com.and04.naturealbum.data.repository.firebase.AlbumRepository
import com.and04.naturealbum.data.repository.firebase.AlbumRepositoryImpl
import com.and04.naturealbum.data.repository.firebase.FriendRepository
import com.and04.naturealbum.data.repository.firebase.UserRepository
import com.and04.naturealbum.data.repository.local.LocalAlbumRepository
import com.and04.naturealbum.data.repository.local.PhotoDetailRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun providerFireStore() = Firebase.firestore

    @Provides
    @Singleton
    fun providerFireStorage() = Firebase.storage

    @Provides
    @Singleton
    fun providerFirebaseDataSource(
        fireStore: FirebaseFirestore,
        fireStorage: FirebaseStorage,
    ): RemoteAlbumDataSource = RemoteAlbumDataSource(fireStore, fireStorage)

    @Provides
    @Singleton
    fun providerFireBaseRepository(
        remoteAlbumDataSource: RemoteAlbumDataSource,
        localDataRepository: PhotoDetailRepository,
        localAlbumRepository: LocalAlbumRepository
    ): AlbumRepository =
        AlbumRepositoryImpl(remoteAlbumDataSource, localDataRepository, localAlbumRepository)

    @Provides
    @Singleton
    fun providerFriendRepository(
        remoteAlbumDataSource: RemoteAlbumDataSource
    ): FriendRepository = FriendRepository(remoteAlbumDataSource)

    @Provides
    @Singleton
    fun providerUserRepository(
        remoteAlbumDataSource: RemoteAlbumDataSource
    ): UserRepository = UserRepository(remoteAlbumDataSource)
}
