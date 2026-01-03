package com.incremax.data.repository

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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

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
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            suspendCancellableCoroutine { continuation ->
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result.user?.let {
                                continuation.resume(AuthResult.Success(it.toAuthUser())) {}
                            } ?: continuation.resume(AuthResult.Error("No user returned")) {}
                        } else {
                            val e = task.exception
                            continuation.resume(AuthResult.Error("${e?.javaClass?.simpleName}: ${e?.message}", e)) {}
                        }
                    }
            }
        } catch (e: Exception) {
            AuthResult.Error("${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            suspendCancellableCoroutine { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result.user?.let {
                                continuation.resume(AuthResult.Success(it.toAuthUser())) {}
                            } ?: continuation.resume(AuthResult.Error("Sign-in failed")) {}
                        } else {
                            val e = task.exception
                            continuation.resume(AuthResult.Error("${e?.javaClass?.simpleName}: ${e?.message}", e)) {}
                        }
                    }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Email sign-in failed", e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            suspendCancellableCoroutine { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result.user?.let {
                                continuation.resume(AuthResult.Success(it.toAuthUser())) {}
                            } ?: continuation.resume(AuthResult.Error("Sign-up failed")) {}
                        } else {
                            val e = task.exception
                            continuation.resume(AuthResult.Error("${e?.javaClass?.simpleName}: ${e?.message}", e)) {}
                        }
                    }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Email sign-up failed", e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(AuthUser("", email, null, null, false, emptyList()))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email", e)
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
