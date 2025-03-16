package com.eysamarin.squadplay.domain.profile

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.User

interface ProfileProvider {
    suspend fun getUserInfo(): User
}

class ProfileProviderImpl(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ProfileProvider {

    override suspend fun getUserInfo(): User {
        val userUid = authRepository.getCurrentUserId()
        val friends = profileRepository.getFriends(userUid)

        return User(
            uid = userUid,
            username = "User",
            email = "user email",
            friends = friends,
        )
    }
}
