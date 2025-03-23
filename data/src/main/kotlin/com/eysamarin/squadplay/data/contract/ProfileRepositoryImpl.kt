package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProfileRepositoryImpl(
    val firestoreDataSource: FirebaseFirestoreDataSource,
) : ProfileRepository {

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

    override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<Friend>> {
        return firestoreDataSource.getGroupsMembersInfoFlow(groups)
    }
}