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
)

data class Group(
    val uid: String,
    val title: String,
    val members: List<String>,
)

data class Friend(
    val uid: String,
    val username: String,
    val groupTitleFrom: String,
    val photoUrl: String?,
)