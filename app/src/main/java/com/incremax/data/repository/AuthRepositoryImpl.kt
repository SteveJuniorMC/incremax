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
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.signInWithCredential(credential).await()
            }
            when {
                result == null -> AuthResult.Error("Sign-in timed out. Please check your connection and try again.")
                else -> result.user?.let { AuthResult.Success(it.toAuthUser()) }
                    ?: AuthResult.Error("No user returned from sign-in")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.toUserFriendlyMessage(), e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            when {
                result == null -> AuthResult.Error("Sign-in timed out. Please check your connection and try again.")
                else -> result.user?.let { AuthResult.Success(it.toAuthUser()) }
                    ?: AuthResult.Error("Sign-in failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.toUserFriendlyMessage(), e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            when {
                result == null -> AuthResult.Error("Sign-up timed out. Please check your connection and try again.")
                else -> result.user?.let { AuthResult.Success(it.toAuthUser()) }
                    ?: AuthResult.Error("Sign-up failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.toUserFriendlyMessage(), e)
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
            AuthResult.Error(e.toUserFriendlyMessage(), e)
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

    private fun Exception.toUserFriendlyMessage(): String {
        val msg = message?.lowercase() ?: ""
        return when {
            msg.contains("network") -> "Network error. Please check your connection."
            msg.contains("email") && msg.contains("already") -> "This email is already in use."
            msg.contains("password") && msg.contains("weak") -> "Password is too weak. Use at least 6 characters."
            msg.contains("invalid") && msg.contains("email") -> "Please enter a valid email address."
            msg.contains("user") && msg.contains("not found") -> "No account found with this email."
            msg.contains("wrong") && msg.contains("password") -> "Incorrect password. Please try again."
            msg.contains("credential") -> "Invalid credentials. Please try again."
            msg.contains("too many") -> "Too many attempts. Please try again later."
            else -> "Authentication failed. Please try again."
        }
    }
}
