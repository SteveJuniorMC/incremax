package com.incremax.domain.repository

import com.incremax.domain.model.AuthResult
import com.incremax.domain.model.AuthState
import com.incremax.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: AuthUser?

    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUpWithEmail(email: String, password: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun signOut()

    fun isSignedIn(): Boolean
}
