package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RepositoryPermissionDeniedTest {

    private class RecordingLogger : AppLogger {
        val errorLogs = mutableListOf<String>()
        val debugLogs = mutableListOf<String>()

        override fun d(tag: String?, message: () -> String) {
            debugLogs.add(message())
        }

        override fun i(tag: String?, message: () -> String) {}

        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}

        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {
            errorLogs.add(message())
        }
    }

    private open class BaseFakeFirestoreDataSource : FirebaseFirestoreDataSource {
        override fun getUserInfoFlow(userId: String): Flow<User?> = flow { emit(null) }
        override fun getUserGroupsFlow(userId: String): Flow<List<Group>> = flow { emit(emptyList()) }
        override suspend fun createNewUserGroup(userId: String, title: String): String = ""
        override suspend fun getGroupInfo(groupId: String): Group? = null
        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flow { emit(emptyList()) }
        override suspend fun saveUserProfile(user: User) {}
        override suspend fun isUserProfileExists(userId: String): Boolean = true
        override suspend fun deleteUserProfile(userId: String) {}
        override suspend fun saveEvent(event: Event): Boolean = true
        override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = flow { emit(emptyList()) }
        override suspend fun subscribeToGroupTopic(groupId: String) {}
        override suspend fun unsubscribeFromGroupTopic(groupId: String) {}
        override suspend fun deleteEvent(eventId: String): Boolean = true
        override suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus) {}
        override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = true
        override suspend fun deleteGroup(groupId: String): Boolean = true
        override suspend fun leaveGroup(userId: String, groupId: String): Boolean = true
        override fun clearListeners() {}
    }

    @Test
    fun `ProfileRepository getUserInfoFlow handles PERMISSION_DENIED cleanly`() = runTest {
        val logger = RecordingLogger()
        val fakeDataSource = object : BaseFakeFirestoreDataSource() {
            override fun getUserInfoFlow(userId: String): Flow<User?> = flow {
                throw FirebaseFirestoreException(
                    "Missing or insufficient permissions",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                )
            }
        }

        val repository = ProfileRepositoryImpl(
            firestoreDataSource = fakeDataSource,
            logger = logger,
        )

        val result = repository.getUserInfoFlow("test_uid").first()

        assertNull(result)
        assertEquals(0, logger.errorLogs.size)
        assertEquals(1, logger.debugLogs.size)
    }

    @Test
    fun `ProfileRepository getGroupsMembersInfoFlow handles PERMISSION_DENIED cleanly`() = runTest {
        val logger = RecordingLogger()
        val fakeDataSource = object : BaseFakeFirestoreDataSource() {
            override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flow {
                throw FirebaseFirestoreException(
                    "Missing or insufficient permissions",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                )
            }
        }

        val repository = ProfileRepositoryImpl(
            firestoreDataSource = fakeDataSource,
            logger = logger,
        )

        val result = repository.getGroupsMembersInfoFlow(listOf(Group("g1", "Group 1", listOf("u1")))).first()

        assertEquals(emptyList<UserGroupSection>(), result)
        assertEquals(0, logger.errorLogs.size)
        assertEquals(1, logger.debugLogs.size)
    }

    @Test
    fun `EventRepository getEventsFlow handles PERMISSION_DENIED cleanly`() = runTest {
        val logger = RecordingLogger()
        val fakeDataSource = object : BaseFakeFirestoreDataSource() {
            override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = flow {
                throw FirebaseFirestoreException(
                    "Missing or insufficient permissions",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                )
            }
        }

        val repository = EventRepositoryImpl(
            firebaseFirestoreDataSource = fakeDataSource,
            logger = logger,
        )

        val result = repository.getEventsFlow(setOf("g1")).first()

        assertEquals(emptyList<Event>(), result)
        assertEquals(0, logger.errorLogs.size)
        assertEquals(1, logger.debugLogs.size)
    }
}
