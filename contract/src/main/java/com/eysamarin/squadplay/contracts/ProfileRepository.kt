package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.Friend

fun interface ProfileRepository {
    suspend fun getFriends(userId: String): List<Friend>
}