package com.eysamarin.squadplay.domain.profile

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.User

interface ProfileProvider {
    suspend fun getUserInfo(): User?
    fun createNewInviteLink(userInviteId: String): String
    suspend fun addFriend(userId: String, inviteId: String): Boolean
    suspend fun getFriendInfoByInviteId(inviteId: String): Friend?
}

class ProfileProviderImpl(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ProfileProvider {

    override suspend fun getUserInfo(): User? {
        val userUid = authRepository.getCurrentUserId()
        val userInfo = profileRepository.getUserInfo(userUid)
        return userInfo
    }

    override fun createNewInviteLink(userInviteId: String): String {
        return "https://evgenysamarin.github.io/invite/$userInviteId"
    }

    override suspend fun getFriendInfoByInviteId(inviteId: String): Friend? {
        return profileRepository.getFriendInfoByInviteId(inviteId)
    }

    override suspend fun addFriend(userId: String, inviteId: String): Boolean {
        return profileRepository.addFriend(userId = userId, inviteId = inviteId)
    }
}
