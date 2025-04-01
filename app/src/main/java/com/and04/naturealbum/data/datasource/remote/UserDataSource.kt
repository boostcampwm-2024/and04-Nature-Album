package com.and04.naturealbum.data.datasource.remote

import com.and04.naturealbum.data.dto.FirestoreUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserDataSource @Inject constructor(
    private val fireStore: FirebaseFirestore,
) {
    suspend fun getUser(uid: String): DocumentSnapshot {
        return fireStore.collection(USER).document(uid).get().await()
    }

    fun setUser(uid: String, firestoreUser: FirestoreUser) {
        fireStore.collection(USER).document(uid).set(firestoreUser)
    }

    suspend fun updateUser(uid: String, token: String) {
        fireStore.collection(USER).document(uid)
            .update(FCM_TOKEN, token)
            .await()
    }

    fun searchUsers(query: String): Query {
        return fireStore.collection(USER)
            .whereGreaterThanOrEqualTo(EMAIL, query)
            .whereLessThanOrEqualTo(EMAIL, query + QUERY_SUFFIX)
    }

    companion object {
        const val USER = "USER"
        const val FCM_TOKEN = "fcmToken"
        private const val EMAIL = "email"
        private const val QUERY_SUFFIX = "\uf8ff"
    }
}
