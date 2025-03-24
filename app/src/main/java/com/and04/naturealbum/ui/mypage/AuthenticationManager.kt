package com.and04.naturealbum.ui.mypage

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.and04.naturealbum.BuildConfig
import com.and04.naturealbum.background.workmanager.SynchronizationWorker
import com.and04.naturealbum.data.repository.firebase.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AuthResponse {
    data class Success(val token: String) : AuthResponse
    data class Error(val message: String) : AuthResponse
}

class AuthenticationManager @Inject constructor(
    private val userRepository: UserRepository,
) {
    private val auth = Firebase.auth

    fun signInWithGoogle(context: Context): Flow<AuthResponse> = callbackFlow {
        try {
            val credential = getCredential(context)
            val firebaseCredential = getFirebaseCredential(credential) ?: return@callbackFlow

            handleFirebaseSignIn(context, firebaseCredential) { authResponse ->
                trySend(authResponse)
            }

        } catch (e: GoogleIdTokenParsingException) {
            trySend(AuthResponse.Error(e.message.toString()))
        } catch (e: Exception) {
            trySend(AuthResponse.Error(e.message.toString()))
        } finally {
            awaitClose()
        }
    }

    private suspend fun getCredential(context: Context): Credential {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.google_web_key)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        return result.credential
    }

    private fun getFirebaseCredential(credential: Credential): AuthCredential? {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(credential.data)

            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )

            return firebaseCredential
        }

        return null
    }

    private fun handleFirebaseSignIn(
        context: Context,
        firebaseCredential: AuthCredential,
        trySend: (AuthResponse) -> Unit,
    ) {
        Log.d("FirebaseSignIn", "Starting sign-in process...")
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
                Log.d("FirebaseSignIn", "Sign-in successful")
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    Log.d("FirebaseSignIn", "User authenticated: UID=${currentUser.uid}")

                    // Firestore에 사용자 데이터 추가
                    val uid = currentUser.uid
                    val displayName = currentUser.displayName
                    val email = currentUser.email!! // Google 로그인만 지원하므로 null 아님
                    val photoUrl = currentUser.photoUrl?.toString()

                    Log.d(
                        "FirebaseSignIn",
                        "User UID=${uid}\n displayName=${displayName}\n email=${email} \n photo=${photoUrl}"
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(
                            "FirebaseSignIn",
                            "Attempting to create user in Firestore...(CoroutineScope(Dispatchers.IO).launch )"
                        )
                        val success =
                            createUserInFirestore(
                                uid = uid,
                                displayName = displayName,
                                email = email,
                                photoUrl = photoUrl
                            )
                        if (success) {
                            Log.d("FirebaseSignIn", "User successfully created in Firestore")
                            updateFcmToken(uid) // FCM Token 생성 및 저장
                            getUserToken { authResponse ->
                                Log.d("FirebaseSignIn", "Token retrieved successfully")
                                trySend(authResponse)
                            }
                        } else {
                            Log.e("FirebaseSignIn", "Failed to create user in Firestore")
                            trySend(AuthResponse.Error("Failed to create user in Firestore"))
                        }
                    }
                } else {
                    Log.e("FirebaseSignIn", "User not authenticated")
                    trySend(AuthResponse.Error("User not authenticated"))
                }
                getUserToken { authResponse -> trySend(authResponse) }
                SynchronizationWorker.runSync(context)
            }
            .addOnFailureListener { failureResult ->
                Log.e("FirebaseSignIn", "Sign-in failed: ${failureResult.message}")
                trySend(
                    AuthResponse.Error(
                        failureResult.message.toString()
                    )
                )
            }
    }

    private fun updateFcmToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        userRepository.saveFcmToken(uid, token)
                            .onSuccess {
                                Log.d("FCM", "FCM token updated successfully via Repository")
                            }
                            .onFailure {
                                Log.e("FCM", "Failed to update FCM token via Repository")
                            }
                    }
                } else {
                    Log.e("FCM", "Failed to fetch FCM token: ${task.exception?.message}")
                }
            }
        }
    }

    private suspend fun createUserInFirestore(
        uid: String,
        displayName: String?,
        email: String,
        photoUrl: String?,
    ): Boolean {
        Log.d("Firestore", "Checking if user exists in Firestore: UID=$uid")

        val result = userRepository.createUserIfNotExists(
            uid = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl
        ).onFailure {
            Log.e("Firestore", "Error creating user in Firestore: ${it.message}")
        }

        return result.isSuccess
    }

    private fun getUserToken(trySend: (AuthResponse) -> Unit) {
        Log.d("FirebaseSignIn", "Retrieving user token...")
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.token?.let { token ->
                    Log.d("FirebaseSignIn", "Token retrieved: $token")
                    trySend(AuthResponse.Success(token))
                }
            } else {
                Log.e("FirebaseSignIn", "Failed to retrieve token")
                trySend(AuthResponse.Error("Failed to retrieve token"))
            }
        }
    }
}
