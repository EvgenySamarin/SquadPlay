package com.eysamarin.squadplay.data.datasource

import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.FRIENDS_DATASET
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.USERS_DATASET
import com.eysamarin.squadplay.models.Friend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface FirebaseFirestoreDataSource {
    suspend fun getFriends(userId: String): List<Friend>
    fun addFriend(userId: String, friendId: String)

    companion object {
        const val USERS_DATASET = "users"
        const val FRIENDS_DATASET = "friends"
    }
}

class FirebaseFirestoreDataSourceImpl(
    private val firebaseFirestore: FirebaseFirestore,
): FirebaseFirestoreDataSource {

    override suspend fun getFriends(userId: String): List<Friend> = firebaseFirestore
        .collection(USERS_DATASET)
        .document(userId)
        .collection(FRIENDS_DATASET)
        .get()
        .await()
        .documents.map {
            Friend(
                uid = it.id,
                username = it.getString("username") ?: ""
            )
        }

    override fun addFriend(userId: String, friendId: String) {
        //update friends from user's side
        firebaseFirestore.collection(USERS_DATASET).document(userId)
            .collection(FRIENDS_DATASET).document(friendId)
            .set(hashMapOf<String, Any>())

        //update friends from friend's side
        firebaseFirestore.collection(USERS_DATASET).document(friendId)
            .collection(FRIENDS_DATASET).document(userId)
            .set(hashMapOf<String, Any>())
    }
}