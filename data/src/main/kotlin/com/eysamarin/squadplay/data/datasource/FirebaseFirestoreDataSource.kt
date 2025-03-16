package com.eysamarin.squadplay.data.datasource

import android.util.Log
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.FRIENDS_DATASET
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.USERS_DATASET
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface FirebaseFirestoreDataSource {
    suspend fun getUserInfo(userId: String): User?
    fun addFriend(userId: String, friendId: String)
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)

    companion object {
        const val USERS_DATASET = "users"
        const val FRIENDS_DATASET = "friends"
    }
}

class FirebaseFirestoreDataSourceImpl(
    private val firebaseFirestore: FirebaseFirestore,
): FirebaseFirestoreDataSource {

    override suspend fun deleteUserProfile(userId: String) {
        Log.d("TAG", "Deleting user data for $userId")
        try {
            val userRef = firebaseFirestore.collection(USERS_DATASET).document(userId)
            val friendsCollectionRef = userRef.collection(FRIENDS_DATASET)
            val friendDocs = getCollectionDocuments(friendsCollectionRef)

            firebaseFirestore.runTransaction { transaction ->
                transaction.delete(userRef)
                friendDocs.forEach { transaction.delete(it.reference) }
            }.await()

            Log.d("TAG", "User data deleted successfully for $userId")
        } catch (e: Exception) {
            println("Error deleting user data for $userId: ${e.message}")
        }
    }

    private suspend fun getCollectionDocuments(
        collectionRef: CollectionReference,
    ): List<DocumentSnapshot> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = collectionRef.get().await()
            querySnapshot.documents
        } catch (e: Exception) {
            Log.e("TAG", "Error getting documents: ${e.message}")
            emptyList()
        }
    }

    override suspend fun saveUserProfile(user: User) {
        val userDataMap = hashMapOf(
            "username" to user.username,
            "email" to user.email,
            "photoUrl" to user.photoUrl,
        )

        firebaseFirestore.collection(USERS_DATASET).document(user.uid)
            .set(userDataMap)
            .addOnSuccessListener {
                Log.d("TAG", "User profile saved successfully")
            }
            .addOnFailureListener {
                Log.e("TAG", "Error saving user profile: ${it.message}")
            }
            .await()
    }

    override suspend fun getUserInfo(userId: String): User? {
        val userDocument = firebaseFirestore
            .collection(USERS_DATASET)
            .document(userId)

        val userData = userDocument.get()
            .addOnFailureListener {
                Log.e("TAG", "Error getting user data: ${it.message}")
            }
            .await().data

        if (userData == null) {
            Log.e("TAG", "User data is null")
            return null
        }

        val friends = userDocument
            .collection(FRIENDS_DATASET)
            .get()
            .addOnFailureListener {
                Log.e("TAG", "Error getting friends: ${it.message}")
            }
            .await()
            .documents.map {
                Friend(
                    uid = it.id,
                    username = it.getString("username") ?: ""
                )
            }

        return User(
            uid = userId,
            username = userData["username"] as String? ?: "User",
            email = userData["email"] as String?,
            photoUrl = userData["photoUrl"] as String?,
            friends = friends
        )
    }

    override fun addFriend(userId: String, friendId: String) {
        //update friends from user's side
        firebaseFirestore.collection(USERS_DATASET).document(userId)
            .collection(FRIENDS_DATASET).document(friendId)
            .set(hashMapOf<String, Any>())
            .addOnFailureListener {
                Log.e("TAG", "Error adding friend for userId: ${it.message}")
            }

        //update friends from friend's side
        firebaseFirestore.collection(USERS_DATASET).document(friendId)
            .collection(FRIENDS_DATASET).document(userId)
            .set(hashMapOf<String, Any>())
            .addOnFailureListener {
                Log.e("TAG", "Error adding user as friend for friendId: ${it.message}")
            }
    }
}