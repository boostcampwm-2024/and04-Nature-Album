package com.and04.naturealbum.data.repository.firebase

import android.net.Uri
import android.util.Log
import com.and04.naturealbum.data.datasource.remote.RemoteAlbumDataSource
import com.and04.naturealbum.data.dto.FirebaseLabel
import com.and04.naturealbum.data.dto.FirebaseLabelResponse
import com.and04.naturealbum.data.dto.FirebasePhotoInfo
import com.and04.naturealbum.data.dto.FirebasePhotoInfoResponse
import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.data.repository.local.LocalAlbumRepository
import com.and04.naturealbum.data.repository.local.PhotoDetailRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

interface AlbumRepository {
    suspend fun getLabelsToList(uid: String): Result<List<FirebaseLabelResponse>>
    suspend fun getPhotosToList(uid: String): Result<List<FirebasePhotoInfoResponse>>
    suspend fun getLabelsToMap(uids: List<String>): Result<Map<String, List<FirebaseLabelResponse>>>
    suspend fun getPhotos(uids: List<String>): Result<Map<String, List<FirebasePhotoInfoResponse>>>

    suspend fun saveImageFile(uid: String, label: String, fileName: String, uri: Uri): Result<Uri>
    suspend fun insertLabel(
        uid: String,
        labelName: String,
        labelData: FirebaseLabel,
    ): Result<Unit>

    suspend fun insertPhotoInfo(
        uid: String,
        fileName: String,
        photoData: FirebasePhotoInfo,
    ): Result<Unit>

    suspend fun deleteImageFile(uid: String, label: Label, fileName: String): Boolean
}

class AlbumRepositoryImpl @Inject constructor(
    private val remoteAlbumDataSource: RemoteAlbumDataSource,
    private val photoDetailRepository: PhotoDetailRepository,
    private val localAlbumRepository: LocalAlbumRepository,
) : AlbumRepository {

    override suspend fun getLabelsToList(uid: String): Result<List<FirebaseLabelResponse>> {
        return runCatching {
            remoteAlbumDataSource
                .getUserLabels(uid)
        }.mapCatching { querySnapshot ->
            querySnapshot
                .documents
                .mapNotNull { document ->
                    document.toObject(FirebaseLabelResponse::class.java)?.copy(
                        labelName = document.id
                    )
                }
        }
    }

    override suspend fun getLabelsToMap(uids: List<String>): Result<Map<String, List<FirebaseLabelResponse>>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val labels = uids.map { uid ->
                    async {
                        getLabelsToList(uid).getOrThrow()
                    }
                }.awaitAll()
                uids.zip(labels).toMap()
            }
        }
    }

    override suspend fun getPhotosToList(uid: String): Result<List<FirebasePhotoInfoResponse>> {
        return runCatching {
            remoteAlbumDataSource
                .getUserPhotos(uid)
        }.mapCatching { querySnapshot ->
            querySnapshot
                .documents
                .mapNotNull { document ->
                    document.toObject(FirebasePhotoInfoResponse::class.java)?.copy(
                        fileName = document.id
                    )
                }
        }
    }

    override suspend fun getPhotos(uids: List<String>): Result<Map<String, List<FirebasePhotoInfoResponse>>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val photos = uids.map { uid ->
                    async {
                        getPhotosToList(uid).getOrThrow()
                    }
                }.awaitAll()
                uids.zip(photos).toMap()
            }
        }
    }

    override suspend fun saveImageFile(
        uid: String,
        label: String,
        fileName: String,
        uri: Uri,
    ): Result<Uri> {
        return runCatching { remoteAlbumDataSource.saveImage(uid, label, fileName, uri) }
    }

    override suspend fun deleteImageFile(uid: String, label: Label, fileName: String): Boolean =
        supervisorScope {
            val exceptionHandler = CoroutineExceptionHandler { _, _ ->
                Log.e("AlbumRepository", "deleteImageFile Error")
            }
            FirebaseLock.deleteMutex.withLock {
                return@supervisorScope if (isFileExist(uid, label.name, fileName)) {
                    val deleteFileJob = async(exceptionHandler) {
                        remoteAlbumDataSource.deleteImage(
                            uid,
                            label,
                            fileName
                        )
                    }

                    val checkAlbumsJob = async(exceptionHandler) {
                        val albums = localAlbumRepository.getAlbumByLabelId(label.id)
                        if (albums.isEmpty()) {
                            remoteAlbumDataSource.deleteUserLabel(uid, label)
                        } else {
                            val albumPresentFileName =
                                photoDetailRepository.getPhotoDetailById(albums[0].photoDetailId).fileName
                            val document =
                                remoteAlbumDataSource.getPhotoInfo(uid, albumPresentFileName)
                            document.toObject(FirebasePhotoInfoResponse::class.java)
                                ?.let { photoInfo ->
                                    remoteAlbumDataSource.setUserLabel(
                                        uid,
                                        label.name,
                                        FirebaseLabel(
                                            backgroundColor = label.backgroundColor,
                                            thumbnailUri = photoInfo.uri,
                                            fileName = document.id,
                                        )
                                    )
                                }
                        }
                    }

                    val deletePhotoJob = async(exceptionHandler) {
                        remoteAlbumDataSource.deleteUserPhoto(
                            uid,
                            fileName
                        )
                    }

                    awaitAll(deleteFileJob, checkAlbumsJob, deletePhotoJob)
                    true
                } else {
                    false
                }
            }
        }

    private suspend fun isFileExist(uid: String, labelName: String, fileName: String): Boolean {
        return withTimeoutOrNull(2_000) {
            while (true) {
                when (remoteAlbumDataSource.checkFileExist(uid, labelName, fileName)) {
                    true -> return@withTimeoutOrNull true
                    false -> delay(500)
                }
            }
        } as Boolean
    }

    override suspend fun insertLabel(
        uid: String,
        labelName: String,
        labelData: FirebaseLabel,
    ): Result<Unit> {
        return runCatching { remoteAlbumDataSource.setUserLabel(uid, labelName, labelData) }
    }

    override suspend fun insertPhotoInfo(
        uid: String,
        fileName: String,
        photoData: FirebasePhotoInfo,
    ): Result<Unit> {
        return FirebaseLock.insertMutex.withLock {
            return@withLock runCatching {
                remoteAlbumDataSource.setUserPhoto(
                    uid,
                    fileName,
                    photoData
                )
            }
        }
    }

    object FirebaseLock {
        val insertMutex = Mutex()
        val deleteMutex = Mutex()
    }
}
