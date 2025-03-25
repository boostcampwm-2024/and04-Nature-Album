package com.and04.naturealbum.data.repository.firebase

import com.and04.naturealbum.data.datasource.remote.UserDataSource
import com.and04.naturealbum.data.dto.FirestoreUser
import com.and04.naturealbum.data.dto.FirestoreUser.Companion.EMPTY
import com.and04.naturealbum.data.dto.FirestoreUser.Companion.UNKNOWN
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDataSource: UserDataSource,
) {
    suspend fun createUserIfNotExists(
        uid: String,
        displayName: String?,
        email: String,
        photoUrl: String?,
    ): Result<Unit> {
        return runCatching {
            val userDoc = userDataSource.getUser(uid)
            if (!userDoc.exists()) {
                val firestoreUser = FirestoreUser(
                    uid = uid,
                    displayName = displayName ?: UNKNOWN,
                    email = email,
                    photoUrl = photoUrl ?: EMPTY
                )
                userDataSource.setUser(uid, firestoreUser)
            }
        }
    }

    suspend fun saveFcmToken(uid: String, token: String): Result<Unit> {
        return runCatching { userDataSource.updateUser(uid, token) }
    }
}
