package com.eysamarin.squadplay.domain.profile

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.User

interface ProfileProvider {
    suspend fun getUserInfo(): User?
    fun createNewInviteLink(userInviteId: String): String
    fun addFriend(userId: String, friendId: String)
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

    override fun addFriend(userId: String, friendId: String) {
        profileRepository.addFriend(userId, friendId)
    }
}
