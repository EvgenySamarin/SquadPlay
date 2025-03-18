package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.User

class ProfileRepositoryImpl(
    val firestoreDataSource: FirebaseFirestoreDataSource,
) : ProfileRepository {

    override suspend fun getUserInfo(userId: String): User? =
        firestoreDataSource.getUserInfo(userId)

    override suspend fun saveUserProfile(user: User) = firestoreDataSource.saveUserProfile(user)
    override suspend fun deleteUserProfile(userId: String) = firestoreDataSource
        .deleteUserProfile(userId)

    override suspend fun addFriend(userId: String, inviteId: String): Boolean = firestoreDataSource
        .addFriend(userId = userId, inviteId = inviteId)

    override suspend fun getFriendInfoByInviteId(inviteId: String): Friend? = firestoreDataSource
        .getFriendInfoByInviteId(inviteId)
}