package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

class ProfileRepositoryImpl(
    val firestoreDataSource: FirebaseFirestoreDataSource,
    private val logger: AppLogger,
) : ProfileRepository {

    override suspend fun isUserProfileExists(userId: String): Boolean =
        firestoreDataSource.isUserProfileExists(userId)

    override fun getUserInfoFlow(userId: String): Flow<User?> = combine(
        firestoreDataSource.getUserInfoFlow(userId),
        firestoreDataSource.getUserGroupsFlow(userId),
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
        logger.e(tag = "ProfileRepository", throwable = it) { "Cannot get user info cause: ${it.message}" }
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
            logger.e(tag = "ProfileRepository", throwable = it) { "Cannot get groups member info cause: ${it.message}" }
        }
}