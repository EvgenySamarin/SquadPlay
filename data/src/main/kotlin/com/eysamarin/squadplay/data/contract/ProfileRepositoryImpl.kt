package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.contracts.SecurityLockoutManager
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

class ProfileRepositoryImpl(
    val firestoreDataSource: FirebaseFirestoreDataSource,
    private val logger: AppLogger,
    private val securityLockoutManager: SecurityLockoutManager? = null,
) : ProfileRepository {

    @Throws(AppErrorException::class)
    override suspend fun isUserProfileExists(userId: String): Boolean = try {
        firestoreDataSource.isUserProfileExists(userId)
    } catch (e: AppErrorException) {
        logger.w(tag = "ProfileRepository", throwable = e) { "App Check attestation failure in isUserProfileExists: ${e.message}" }
        securityLockoutManager?.triggerLockout()
        throw e
    }

    override fun getUserInfoFlow(userId: String): Flow<User?> = combine(
        firestoreDataSource.getUserInfoFlow(userId),
        firestoreDataSource.getUserGroupsFlow(userId).onStart { emit(emptyList()) },
    ) { user, groups ->
        if (groups.isEmpty()) {
            user
        } else {
            val groupsExcludingCurrentUserMember = groups.map {
                it.copy(members = it.members.filter { it != userId })
            }
            user?.copy(groups = groupsExcludingCurrentUserMember)
        }
    }.catch {
        if (it is AppErrorException) {
            logger.w(tag = "ProfileRepository", throwable = it) { "App Check attestation failure in getUserInfoFlow: ${it.message}" }
            securityLockoutManager?.triggerLockout()
            throw it
        } else if (it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            logger.d(tag = "ProfileRepository") { "Permission denied for user info: ${it.message}" }
            emit(null)
        } else {
            logger.e(tag = "ProfileRepository", throwable = it) { "Cannot get user info cause: ${it.message}" }
            throw it
        }
    }

    override suspend fun saveUserProfile(user: User) = firestoreDataSource.saveUserProfile(user)
    override suspend fun deleteUserProfile(userId: String) = firestoreDataSource
        .deleteUserProfile(userId)

    override suspend fun createNewUserGroup(userId: String, title: String): String = firestoreDataSource
        .createNewUserGroup(userId, title)

    override suspend fun joinGroup(userId: String, groupId: String): Boolean = firestoreDataSource
        .joinGroup(userId = userId, groupId = groupId)
        .also { isSuccess ->
            if (isSuccess) {
                firestoreDataSource.subscribeToGroupTopic(groupId)
            }
        }

    override suspend fun getGroupInfo(groupId: String): Group? = firestoreDataSource
        .getGroupInfo(groupId)

    override fun getGroupsMembersInfoFlow(
        groups: List<Group>
    ): Flow<List<UserGroupSection>> = firestoreDataSource.getGroupsMembersInfoFlow(groups)
        .catch {
            if (it is AppErrorException) {
                logger.w(tag = "ProfileRepository", throwable = it) { "App Check attestation failure in getGroupsMembersInfoFlow: ${it.message}" }
                securityLockoutManager?.triggerLockout()
                throw it
            } else if (it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                logger.d(tag = "ProfileRepository") { "Permission denied for groups member info: ${it.message}" }
                emit(emptyList())
            } else {
                logger.e(tag = "ProfileRepository", throwable = it) { "Cannot get groups member info cause: ${it.message}" }
            }
        }

    override suspend fun renameGroup(groupId: String, newTitle: String): Boolean =
        firestoreDataSource.renameGroup(groupId = groupId, newTitle = newTitle)

    override suspend fun deleteGroup(groupId: String): Boolean =
        firestoreDataSource.deleteGroup(groupId = groupId)

    override suspend fun leaveGroup(userId: String, groupId: String): Boolean =
        firestoreDataSource.leaveGroup(userId = userId, groupId = groupId)

    override suspend fun updateNickname(userId: String, nickname: String): Boolean =
        firestoreDataSource.updateNickname(userId = userId, nickname = nickname)
}