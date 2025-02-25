package com.and04.naturealbum.data.repository.firebase

import com.and04.naturealbum.data.datasource.FirebaseDataSource
import com.and04.naturealbum.data.dto.FirestoreUser
import com.and04.naturealbum.data.dto.FirestoreUser.Companion.EMPTY
import com.and04.naturealbum.data.dto.FirestoreUser.Companion.UNKNOWN
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) {
    suspend fun createUserIfNotExists(
        uid: String,
        displayName: String?,
        email: String,
        photoUrl: String?,
    ): Result<Unit> {
        return runCatching {
            val userDoc = firebaseDataSource.getUser(uid)
            if (!userDoc.exists()) {
                val firestoreUser = FirestoreUser(
                    uid = uid,
                    displayName = displayName ?: UNKNOWN,
                    email = email,
                    photoUrl = photoUrl ?: EMPTY
                )
                firebaseDataSource.setUser(uid, firestoreUser)
            }
        }
    }

    suspend fun saveFcmToken(uid: String, token: String): Result<Unit> {
        return runCatching { firebaseDataSource.updateUser(uid, token) }
    }
}
