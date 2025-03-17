package com.eysamarin.squadplay.models

data class User(
    val uid: String,
    val inviteId: String,
    val username: String,
    val email: String?,
    val photoUrl: String?,
    val friends: List<Friend>
)

data class Friend(
    val uid: String,
    val username: String,
)