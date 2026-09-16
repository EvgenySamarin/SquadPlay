package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.FirebaseAuthManager
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositorySignOutTest {

    @Test
    fun `signOut calls clearListeners before firebaseAuthManager signOut`() = runTest {
        val executionOrder = mutableListOf<String>()

        val fakeFirestoreDataSource = object : FirebaseFirestoreDataSource {
            override fun getUserInfoFlow(userId: String): Flow<User?> = emptyFlow()
            override fun getUserGroupsFlow(userId: String): Flow<List<Group>> = emptyFlow()
            override suspend fun createNewUserGroup(userId: String, title: String): String = ""
            override suspend fun getGroupInfo(groupId: String): Group? = null
            override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
            override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = emptyFlow()
            override suspend fun saveUserProfile(user: User) {}
            override suspend fun isUserProfileExists(userId: String): Boolean = true
            override suspend fun deleteUserProfile(userId: String) {}
            override suspend fun saveEvent(event: Event): Boolean = true
            override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = emptyFlow()
            override suspend fun subscribeToGroupTopic(groupId: String) {}
            override suspend fun unsubscribeFromGroupTopic(groupId: String) {}
            override suspend fun deleteEvent(eventId: String): Boolean = true
            override suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus) {}
            override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = true
            override suspend fun deleteGroup(groupId: String): Boolean = true
            override suspend fun leaveGroup(userId: String, groupId: String): Boolean = true
            override suspend fun updateNickname(userId: String, nickname: String): Boolean = true

            override fun clearListeners() {
                executionOrder.add("clearListeners")
            }
        }

        val fakeAuthManager = object : FirebaseAuthManager {
            override suspend fun signInWithGoogle(): User? = null
            override suspend fun signInWithEmailPassword(email: String, password: String): UiState<User> = UiState.Empty
            override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<User> = UiState.Empty
            override suspend fun signOut(): Boolean {
                executionOrder.add("firebaseAuthSignOut")
                return true
            }
            override fun getUserUid(): String? = "test_user_id"
            override fun getCurrentUserId(): String = "test_user_id"
        }

        val fakeProfileRepository = object : ProfileRepository {
            override suspend fun isUserProfileExists(userId: String): Boolean = true
            override fun getUserInfoFlow(userId: String): Flow<User?> = emptyFlow()
            override suspend fun saveUserProfile(user: User) {}
            override suspend fun deleteUserProfile(userId: String) {}
            override suspend fun createNewUserGroup(userId: String, title: String): String = ""
            override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
            override suspend fun getGroupInfo(groupId: String): Group? = null
            override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = emptyFlow()
            override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = true
            override suspend fun deleteGroup(groupId: String): Boolean = true
            override suspend fun leaveGroup(userId: String, groupId: String): Boolean = true
            override suspend fun updateNickname(userId: String, nickname: String): Boolean = true
        }

        val fakeLogger = object : AppLogger {
            override fun d(tag: String?, message: () -> String) {}
            override fun i(tag: String?, message: () -> String) {}
            override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
            override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
        }

        val repository = AuthRepositoryImpl(
            firebaseAuthManager = fakeAuthManager,
            profileRepository = fakeProfileRepository,
            firestoreDataSource = fakeFirestoreDataSource,
            logger = fakeLogger,
        )

        val result = repository.signOut()

        assertTrue(result)
        assertEquals(listOf("clearListeners", "firebaseAuthSignOut"), executionOrder)
    }
}
