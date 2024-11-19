package com.and04.naturealbum.data.repository

import android.net.Uri
import com.and04.naturealbum.data.dto.FirebaseFriend
import com.and04.naturealbum.data.dto.FirebaseFriendRequest
import com.and04.naturealbum.data.dto.FirebaseLabel
import com.and04.naturealbum.data.dto.FirebasePhotoInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject

interface FireBaseRepository {

    // CREATE
    suspend fun createUserIfNotExists(
        uid: String,
        displayName: String?,
        email: String,
        photoUrl: String?
    ): Boolean

    //SELECT
    suspend fun getLabel(uid: String, label: String): Task<DocumentSnapshot>
    suspend fun getLabels(uid: String): Task<QuerySnapshot>
    suspend fun getFriendRequests(uid: String): Task<QuerySnapshot>
    suspend fun getFriends(uid: String): Task<QuerySnapshot>

    //INSERT
    suspend fun saveImageFile(uid: String, label: String, fileName: String, uri: Uri): Uri
    suspend fun insertLabel(
        uid: String,
        labelName: String,
        labelData: FirebaseLabel
    ): Boolean

    suspend fun insertPhotoInfo(
        uid: String,
        fileName: String,
        photoData: FirebasePhotoInfo
    ): Boolean

    suspend fun sendFriendRequest(uid: String, targetUid: String): Boolean
    suspend fun acceptFriendRequest(uid: String, targetUid: String): Boolean
    suspend fun rejectFriendRequest(uid: String, targetUid: String): Boolean

    //UPDATE

}

class FireBaseRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val fireStorage: FirebaseStorage,
) : FireBaseRepository {

    override suspend fun createUserIfNotExists(
        uid: String,
        displayName: String?,
        email: String,
        photoUrl: String?
    ): Boolean {
        return try {
            val userDoc = fireStore.collection(USER).document(uid).get().await()
            if (!userDoc.exists()) {
                fireStore.collection(USER).document(uid).set(
                    mapOf(
                        "displayName" to (displayName ?: "NoName User"),
                        "email" to email,
                        "photoUrl" to (photoUrl ?: "")
                    )
                ).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getLabel(uid: String, label: String): Task<DocumentSnapshot> {

        return fireStore.collection(USER).document(uid).collection(LABEL).document(label).get()
    }

    override suspend fun getLabels(uid: String): Task<QuerySnapshot> {

        return fireStore.collection(USER).document(uid).collection(LABEL).get()
    }

    override suspend fun getFriends(uid: String): Task<QuerySnapshot> {
        return fireStore.collection(USER).document().collection(FRIENDS).get()
    }

    override suspend fun getFriendRequests(uid: String): Task<QuerySnapshot> {
        return fireStore.collection(USER).document().collection(FRIEND_REQUESTS).get()
    }

    override suspend fun saveImageFile(
        uid: String,
        label: String,
        fileName: String,
        uri: Uri,
    ): Uri {
        val task = fireStorage.getReference("$uid/$label/$fileName").putFile(uri).await()
        return task.storage.downloadUrl.await()
    }

    override suspend fun insertLabel(
        uid: String,
        labelName: String,
        labelData: FirebaseLabel
    ): Boolean {
        var requestSuccess = false
        fireStore.collection(USER).document(uid).collection(LABEL).document(labelName)
            .set(labelData)
            .addOnSuccessListener {
                requestSuccess = true
            }.await()

        return requestSuccess
    }

    override suspend fun insertPhotoInfo(
        uid: String,
        fileName: String,
        photoData: FirebasePhotoInfo
    ): Boolean {
        var requestSuccess = false

        fireStore.collection(USER).document(uid).collection(PHOTOS).document(fileName)
            .set(photoData)
            .addOnSuccessListener {
                requestSuccess = true
            }.await()

        return requestSuccess
    }

    // 친구 요청 보냈을 경우
    override suspend fun sendFriendRequest(uid: String, targetUid: String): Boolean {
        val requestTime = LocalDateTime.now()
        val friendRequest = FirebaseFriendRequest(requestedAt = requestTime, status = "sent")
        val targetFriendRequest = friendRequest.copy(status = "received")

        return try {
            fireStore.runTransaction { transaction ->
                transaction.set(
                    fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
                        .document(targetUid),
                    friendRequest
                )
                transaction.set(
                    fireStore.collection(USER).document(targetUid).collection(FRIEND_REQUESTS)
                        .document(uid),
                    friendRequest
                )
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 수락했을 경우
    override suspend fun acceptFriendRequest(uid: String, targetUid: String): Boolean {
        val addedTime = LocalDateTime.now()
        val friendData = FirebaseFriend(addedAt = addedTime)

        return try {
            fireStore.runTransaction { transaction ->
                // 서로의 친구 리스트에 추가
                transaction.set(
                    fireStore.collection(USER).document(uid).collection(FRIENDS)
                        .document(targetUid),
                    friendData
                )
                transaction.set(
                    fireStore.collection(USER).document(targetUid).collection(FRIENDS)
                        .document(uid),
                    friendData
                )
                // 서로의 요청 상태 제거
                transaction.delete(
                    fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
                        .document(targetUid)
                )
                transaction.delete(
                    fireStore.collection(USER).document(targetUid).collection(FRIEND_REQUESTS)
                        .document(uid)
                )

            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 거절했을 경우
    override suspend fun rejectFriendRequest(uid: String, targetUid: String): Boolean {
        return try {
            fireStore.runTransaction { transaction ->
                // 서로의 요청 상태 제거
                transaction.delete(
                    fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
                        .document(targetUid)
                )
                transaction.delete(
                    fireStore.collection(USER).document(targetUid).collection(FRIEND_REQUESTS)
                        .document(uid)
                )
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val USER = "USER"
        private const val LABEL = "LABEL"
        private const val PHOTOS = "PHOTOS"
        private const val FRIENDS = "FRIENDS"
        private const val FRIEND_REQUESTS = "FRIEND_REQUESTS"
    }
}
