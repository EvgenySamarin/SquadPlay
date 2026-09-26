package com.eysamarin.squadplay.domain.profile

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface ProfileProvider {
    fun getUserInfoFlow(): Flow<User?>
    fun createNewInviteLink(inviteGroupId: String): String
    suspend fun joinGroup(userId: String, groupId: String): Boolean
    suspend fun getGroupInfo(groupId: String): Group?

    /**
     * @return created group uid
     */
    suspend fun createNewUserGroup(userId: String, title: String = "Friends"): String
    fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>>
    suspend fun renameGroup(groupId: String, newTitle: String): Boolean
    suspend fun deleteGroup(groupId: String): Boolean
    suspend fun leaveGroup(userId: String, groupId: String): Boolean
    suspend fun updateNickname(userId: String, nickname: String): Boolean
}

class ProfileProviderImpl(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ProfileProvider {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserInfoFlow(): Flow<User?> = authRepository.getCurrentUserIdFlow()
        .flatMapLatest { userUid ->
            if (userUid.isNullOrBlank()) {
                flowOf(null)
            } else {
                profileRepository.getUserInfoFlow(userUid)
            }
        }

    override suspend fun createNewUserGroup(userId: String, title: String): String {
        return profileRepository.createNewUserGroup(userId, title)
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

    override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> {
        return profileRepository.getGroupsMembersInfoFlow(groups)
    }

    override suspend fun renameGroup(groupId: String, newTitle: String): Boolean {
        return profileRepository.renameGroup(groupId = groupId, newTitle = newTitle)
    }

    override suspend fun deleteGroup(groupId: String): Boolean {
        return profileRepository.deleteGroup(groupId = groupId)
    }

    override suspend fun leaveGroup(userId: String, groupId: String): Boolean {
        return profileRepository.leaveGroup(userId = userId, groupId = groupId)
    }

    override suspend fun updateNickname(userId: String, nickname: String): Boolean {
        return profileRepository.updateNickname(userId = userId, nickname = nickname)
    }
}
