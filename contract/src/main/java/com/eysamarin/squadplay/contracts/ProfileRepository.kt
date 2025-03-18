package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.User

interface ProfileRepository {
    suspend fun getUserInfo(userId: String): User?
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)
    fun addFriend(userId: String, friendId: String)
}