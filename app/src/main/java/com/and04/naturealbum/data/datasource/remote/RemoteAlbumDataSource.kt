package com.and04.naturealbum.data.datasource.remote

import android.net.Uri
import com.and04.naturealbum.data.datasource.remote.UserDataSource.Companion.USER
import com.and04.naturealbum.data.dto.FirebaseLabel
import com.and04.naturealbum.data.dto.FirebasePhotoInfo
import com.and04.naturealbum.data.localdata.room.Label
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteAlbumDataSource @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val fireStorage: FirebaseStorage,
) {
    suspend fun saveImage(
        uid: String,
        label: String,
        fileName: String,
        uri: Uri,
    ): Uri {
        val task = fireStorage.getReference("$uid/$label/$fileName").putFile(uri).await()
        return task.storage.downloadUrl.await()
    }

    suspend fun deleteImage(uid: String, label: Label, fileName: String) {
        fireStorage.getReference("$uid/${label.name}/$fileName").delete().await()
    }

    suspend fun getUserLabels(uid: String): QuerySnapshot {
        return fireStore.collection(USER).document(uid).collection(LABEL).get().await()
    }

    suspend fun setUserLabel(
        uid: String,
        labelName: String,
        labelData: FirebaseLabel,
    ) {
        fireStore.collection(USER).document(uid).collection(LABEL).document(labelName)
            .set(labelData).await()
    }

    suspend fun deleteUserLabel(uid: String, label: Label) {
        fireStore.collection(USER).document(uid).collection(LABEL)
            .document(label.name)
            .delete()
            .await()
    }

    suspend fun getUserPhotos(uid: String): QuerySnapshot {
        return fireStore.collection(USER).document(uid)
            .collection(PHOTOS)
            .get()
            .await()
    }

    suspend fun getPhotoInfo(uid: String, fileName: String): DocumentSnapshot {
        return fireStore.collection(USER).document(uid)
            .collection(PHOTOS)
            .document(fileName)
            .get()
            .await()
    }

    suspend fun setUserPhoto(
        uid: String,
        fileName: String,
        photoData: FirebasePhotoInfo,
    ) {
        fireStore.collection(USER).document(uid)
            .collection(PHOTOS).document(fileName)
            .set(photoData)
            .await()
    }

    suspend fun deleteUserPhoto(uid: String, fileName: String) {
        fireStore.collection(USER).document(uid).collection(PHOTOS)
            .document(fileName)
            .delete()
            .await()
    }

    suspend fun checkFileExist(uid: String, labelName: String, fileName: String): Boolean {
        try {
            fireStorage.getReference("$uid/${labelName}/$fileName").metadata.await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    companion object {
        private const val LABEL = "LABEL"
        private const val PHOTOS = "PHOTOS"
    }
}
