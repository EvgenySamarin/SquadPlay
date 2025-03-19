package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserInfoFlow(userId: String): Flow<User?>
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)
    suspend fun getGroupInfo(groupId: String): Group?
    suspend fun joinGroup(userId: String, groupId: String): Boolean
    suspend fun createNewUserGroup(userId: String, title: String): String
    fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<Friend>>
}