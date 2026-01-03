package com.incremax.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.incremax.domain.model.AuthResult
import com.incremax.domain.model.AuthState
import com.incremax.domain.model.AuthUser
import com.incremax.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"
private const val AUTH_TIMEOUT_MS = 30_000L

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val authState: Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                trySend(AuthState.Authenticated(user.toAuthUser()))
            } else {
                trySend(AuthState.NotAuthenticated)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toAuthUser()

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        Log.d(TAG, "signInWithGoogle: starting with token=${idToken.take(20)}...")
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Log.d(TAG, "signInWithGoogle: calling Firebase...")
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.signInWithCredential(credential).await()
            }
            Log.d(TAG, "signInWithGoogle: Firebase returned, result=$result, user=${result?.user}")
            when {
                result == null -> {
                    Log.e(TAG, "signInWithGoogle: TIMEOUT after ${AUTH_TIMEOUT_MS}ms")
                    AuthResult.Error("Sign-in timed out. Please check your connection and try again.")
                }
                result.user != null -> {
                    Log.d(TAG, "signInWithGoogle: SUCCESS user=${result.user?.uid}")
                    AuthResult.Success(result.user!!.toAuthUser())
                }
                else -> {
                    Log.e(TAG, "signInWithGoogle: no user returned")
                    AuthResult.Error("No user returned from sign-in")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogle: EXCEPTION ${e.javaClass.simpleName}: ${e.message}", e)
            AuthResult.Error("${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        Log.d(TAG, "signInWithEmail: starting for $email")
        return try {
            Log.d(TAG, "signInWithEmail: calling Firebase...")
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            Log.d(TAG, "signInWithEmail: Firebase returned, result=$result, user=${result?.user}")
            when {
                result == null -> {
                    Log.e(TAG, "signInWithEmail: TIMEOUT after ${AUTH_TIMEOUT_MS}ms")
                    AuthResult.Error("Sign-in timed out. Please check your connection and try again.")
                }
                result.user != null -> {
                    Log.d(TAG, "signInWithEmail: SUCCESS user=${result.user?.uid}")
                    AuthResult.Success(result.user!!.toAuthUser())
                }
                else -> {
                    Log.e(TAG, "signInWithEmail: no user returned")
                    AuthResult.Error("Sign-in failed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail: EXCEPTION ${e.javaClass.simpleName}: ${e.message}", e)
            AuthResult.Error("${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        Log.d(TAG, "signUpWithEmail: starting for $email")
        return try {
            Log.d(TAG, "signUpWithEmail: calling Firebase...")
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            Log.d(TAG, "signUpWithEmail: Firebase returned, result=$result, user=${result?.user}")
            when {
                result == null -> {
                    Log.e(TAG, "signUpWithEmail: TIMEOUT after ${AUTH_TIMEOUT_MS}ms")
                    AuthResult.Error("Sign-up timed out. Please check your connection and try again.")
                }
                result.user != null -> {
                    Log.d(TAG, "signUpWithEmail: SUCCESS user=${result.user?.uid}")
                    AuthResult.Success(result.user!!.toAuthUser())
                }
                else -> {
                    Log.e(TAG, "signUpWithEmail: no user returned")
                    AuthResult.Error("Sign-up failed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail: EXCEPTION ${e.javaClass.simpleName}: ${e.message}", e)
            AuthResult.Error("${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.sendPasswordResetEmail(email).await()
            }
            if (result != null) {
                AuthResult.Success(AuthUser("", email, null, null, false, emptyList()))
            } else {
                AuthResult.Error("Request timed out. Please check your connection and try again.")
            }
        } catch (e: Exception) {
            AuthResult.Error("${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified,
            providerIds = providerData.map { it.providerId }
        )
    }
}
