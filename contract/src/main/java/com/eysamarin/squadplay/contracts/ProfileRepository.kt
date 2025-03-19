package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserInfoFlow(userId: String): Flow<User?>
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)
    suspend fun getFriendInfoByInviteId(inviteId: String): Friend?
    suspend fun addFriend(userId: String, inviteId: String): Boolean
}