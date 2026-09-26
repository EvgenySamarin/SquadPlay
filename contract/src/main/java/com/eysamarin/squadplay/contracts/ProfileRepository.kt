package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    @Throws(AppErrorException::class)
    suspend fun isUserProfileExists(userId: String): Boolean
    fun getUserInfoFlow(userId: String): Flow<User?>
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)
    suspend fun getGroupInfo(groupId: String): Group?
    suspend fun joinGroup(userId: String, groupId: String): Boolean
    suspend fun createNewUserGroup(userId: String, title: String): String
    fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>>
    suspend fun renameGroup(groupId: String, newTitle: String): Boolean
    suspend fun deleteGroup(groupId: String): Boolean
    suspend fun leaveGroup(userId: String, groupId: String): Boolean
    suspend fun updateNickname(userId: String, nickname: String): Boolean
}