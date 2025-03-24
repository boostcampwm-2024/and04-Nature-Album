package com.and04.naturealbum.data.datasource.remote

import com.and04.naturealbum.data.datasource.remote.UserDataSource.Companion.USER
import com.and04.naturealbum.data.dto.FirebaseFriend
import com.and04.naturealbum.data.dto.FirebaseFriendRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FriendDataSource @Inject constructor(
    private val fireStore: FirebaseFirestore,
) {
    fun getUserFriends(uid: String): CollectionReference {
        return fireStore.collection(USER).document(uid).collection(FRIENDS)
    }

    fun getReceivedFriendRequests(uid: String): CollectionReference {
        return fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
    }

    fun getFriendRequestDoc(uid: String, targetUid: String): DocumentReference {
        return fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
            .document(targetUid)
    }

    fun getFriendDoc(uid: String, targetUid: String): DocumentReference {
        return fireStore.collection(USER).document(uid).collection(FRIENDS).document(targetUid)
    }

    suspend fun setTransactionFriendRequest(
        uid: String,
        targetUid: String,
        friendRequest: FirebaseFriendRequest,
        targetFriendRequest: FirebaseFriendRequest,
    ) {
        fireStore.runTransaction { transaction ->
            transaction.set(
                getFriendRequestDoc(uid, targetUid),
                friendRequest
            )

            transaction.set(
                getFriendRequestDoc(targetUid, uid),
                targetFriendRequest
            )
        }.await()
    }

    suspend fun deleteTransactionFriendRequest(
        uid: String,
        targetUid: String,
    ) {
        fireStore.runTransaction { transaction ->
            transaction.delete(
                fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
                    .document(targetUid)
            )

            transaction.delete(
                fireStore.collection(USER).document(targetUid).collection(FRIEND_REQUESTS)
                    .document(uid)
            )
        }.await()
    }

    suspend fun acceptTransactionFriendRequest(
        uid: String,
        targetUid: String,
        uidFriendData: FirebaseFriend,
        targetUidFriendData: FirebaseFriend,
    ) {
        fireStore.runTransaction { transaction ->
            transaction.set(
                fireStore.collection(USER).document(uid).collection(FRIENDS)
                    .document(targetUid),
                uidFriendData
            )

            transaction.set(
                fireStore.collection(USER).document(targetUid).collection(FRIENDS)
                    .document(uid),
                targetUidFriendData
            )

            transaction.delete(
                fireStore.collection(USER).document(uid).collection(FRIEND_REQUESTS)
                    .document(targetUid)
            )

            transaction.delete(
                fireStore.collection(USER).document(targetUid).collection(FRIEND_REQUESTS)
                    .document(uid)
            )
        }.await()
    }

    companion object{
        private const val FRIENDS = "FRIENDS"
        private const val FRIEND_REQUESTS = "FRIEND_REQUESTS"
    }
}
