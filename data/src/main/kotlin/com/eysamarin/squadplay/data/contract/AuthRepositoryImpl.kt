package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.contracts.SecurityLockoutManager
import com.eysamarin.squadplay.data.FirebaseAuthManager
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User

class AuthRepositoryImpl(
    val firebaseAuthManager: FirebaseAuthManager,
    val profileRepository: ProfileRepository,
    val firestoreDataSource: FirebaseFirestoreDataSource,
    private val logger: AppLogger,
    private val securityLockoutManager: SecurityLockoutManager? = null,
) : AuthRepository {

    override fun getCurrentUserId(): String? = try {
        firebaseAuthManager.getCurrentUserId()
    } catch (userNotSignIn: IllegalStateException) {
        logger.w(tag = "AuthRepository", throwable = userNotSignIn) { "Cannot get current user id, cause: ${userNotSignIn.message}" }
        null
    }

    @Throws(AppErrorException::class)
    override suspend fun isUserExists(): Boolean = try {
        firebaseAuthManager.getUserUid()?.let {
            profileRepository.isUserProfileExists(it)
        } == true
    } catch (e: AppErrorException) {
        logger.w(tag = "AuthRepository", throwable = e) { "App Check attestation failure in isUserExists: ${e.message}" }
        securityLockoutManager?.triggerLockout()
        throw e
    }

    override suspend fun signInWithGoogle() = firebaseAuthManager.signInWithGoogle()

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String,
    ): UiState<User> = firebaseAuthManager.signUpWithEmailPassword(email, password)

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String,
    ): UiState<User> = firebaseAuthManager.signInWithEmailPassword(email, password)

    override suspend fun signOut(): Boolean {
        firestoreDataSource.clearListeners()
        return firebaseAuthManager.signOut()
    }
}