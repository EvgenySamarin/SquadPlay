package com.eysamarin.squadplay.domain.profile

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.flow.Flow

interface ProfileProvider {
    fun getUserInfoFlow(): Flow<User?>
    fun createNewInviteLink(inviteGroupId: String): String
    suspend fun joinGroup(userId: String, groupId: String): Boolean
    suspend fun getGroupInfo(groupId: String): Group?

    /**
     * @return created group uid
     */
    suspend fun createNewUserGroup(userId: String): String
    fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<Friend>>
}

class ProfileProviderImpl(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ProfileProvider {

    override fun getUserInfoFlow(): Flow<User?> {
        val userUid = authRepository.getCurrentUserId()
        return profileRepository.getUserInfoFlow(userUid)
    }

    override suspend fun createNewUserGroup(userId: String): String {
        return profileRepository.createNewUserGroup(userId, "Friends")
    }

    override fun createNewInviteLink(inviteGroupId: String): String {
        return "https://evgenysamarin.github.io/invite/$inviteGroupId"
    }

    override suspend fun getGroupInfo(groupId: String): Group? {
        return profileRepository.getGroupInfo(groupId)
    }

    override suspend fun joinGroup(userId: String, groupId: String): Boolean {
        return profileRepository.joinGroup(userId = userId, groupId = groupId)
    }

    override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<Friend>> {
        return profileRepository.getGroupsMembersInfoFlow(groups)
    }
}
