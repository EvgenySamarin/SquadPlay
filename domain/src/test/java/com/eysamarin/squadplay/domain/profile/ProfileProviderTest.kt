package com.eysamarin.squadplay.domain.profile

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileProviderTest {

    private class FakeAuthRepository(
        val userIdFlow: MutableSharedFlow<String?> = MutableSharedFlow()
    ) : AuthRepository {
        override suspend fun signInWithGoogle(): User? = null
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<User> = UiState.Empty
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<User> = UiState.Empty
        override suspend fun signOut(): Boolean = true
        override fun getCurrentUserId(): String? = null
        override fun getCurrentUserIdFlow(): Flow<String?> = userIdFlow
        override suspend fun isUserExists(): Boolean = true
    }

    private class FakeProfileRepository : ProfileRepository {
        val users = mutableMapOf<String, User>()

        override suspend fun isUserProfileExists(userId: String): Boolean = users.containsKey(userId)
        override fun getUserInfoFlow(userId: String): Flow<User?> = flowOf(users[userId])
        override suspend fun saveUserProfile(user: User) {
            users[user.uid] = user
        }
        override suspend fun deleteUserProfile(userId: String) {
            users.remove(userId)
        }
        override suspend fun createNewUserGroup(userId: String, title: String): String = "group-1"
        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = null
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = emptyFlow()
        override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = true
        override suspend fun deleteGroup(groupId: String): Boolean = true
        override suspend fun leaveGroup(userId: String, groupId: String): Boolean = true
        override suspend fun updateNickname(userId: String, nickname: String): Boolean = true
    }

    @Test
    fun `getUserInfoFlow reactively switches user when auth userId emits new id and null on sign out`() = runTest {
        val authRepo = FakeAuthRepository()
        val profileRepo = FakeProfileRepository()
        val user1 = User(uid = "uid1", username = "Alice", email = "alice@example.com", photoUrl = null, groups = emptyList())
        val user2 = User(uid = "uid2", username = "Bob", email = "bob@example.com", photoUrl = null, groups = emptyList())
        profileRepo.users["uid1"] = user1
        profileRepo.users["uid2"] = user2

        val provider = ProfileProviderImpl(
            profileRepository = profileRepo,
            authRepository = authRepo,
        )

        val emissions = mutableListOf<User?>()
        val job = kotlinx.coroutines.launch {
            provider.getUserInfoFlow().collect { emissions.add(it) }
        }

        authRepo.userIdFlow.emit("uid1")
        testScheduler.runCurrent()
        assertEquals(listOf(user1), emissions)

        authRepo.userIdFlow.emit(null)
        testScheduler.runCurrent()
        assertEquals(listOf(user1, null), emissions)

        authRepo.userIdFlow.emit("uid2")
        testScheduler.runCurrent()
        assertEquals(listOf(user1, null, user2), emissions)

        job.cancel()
    }
}
