package com.eysamarin.squadplay.models

/**
 * @property groups calculated field, based on firestore groups collection
 */
data class User(
    val uid: String,
    val username: String,
    val email: String?,
    val photoUrl: String?,
    val groups: List<Group>,
    val nickname: String? = null,
    val lastEventCreatedAt: Long? = null,
)

data class Group(
    val uid: String,
    val title: String,
    val members: List<String>,
    val ownerId: String? = null,
)

data class Friend(
    val uid: String,
    val username: String,
    val groupTitleFrom: String,
    val photoUrl: String?,
    val nickname: String? = null,
)

data class UserGroupSection(
    val groupId: String,
    val title: String,
    val members: List<Friend>,
    val ownerId: String? = null,
)