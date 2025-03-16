package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSource
import com.eysamarin.squadplay.models.Friend

class ProfileRepositoryImpl(
    val firebaseDatabaseDataSource: FirebaseDatabaseDataSource,
) : ProfileRepository {

    override suspend fun getFriends(userId: String): List<Friend> =
        firebaseDatabaseDataSource.getFriends(userId)
}