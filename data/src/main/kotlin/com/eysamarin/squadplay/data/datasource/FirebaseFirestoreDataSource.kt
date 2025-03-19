package com.eysamarin.squadplay.data.datasource

import android.util.Log
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.FRIENDS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.USERS_COLLECTION
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface FirebaseFirestoreDataSource {
    fun getUserInfoFlow(userId: String): Flow<User?>
    suspend fun addFriend(userId: String, inviteId: String): Boolean
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)
    suspend fun getFriendInfoByInviteId(inviteId: String): Friend?

    companion object {
        const val USERS_COLLECTION = "users"
        const val FRIENDS_COLLECTION = "friends"
    }
}

class FirebaseFirestoreDataSourceImpl(
    private val firebaseFirestore: FirebaseFirestore,
): FirebaseFirestoreDataSource {

    override suspend fun deleteUserProfile(userId: String) {
        Log.d("TAG", "Deleting user data for $userId")
        try {
            val userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId)
            val friendsCollectionRef = userRef.collection(FRIENDS_COLLECTION)
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
            "inviteId" to user.inviteId,
            "username" to user.username,
            "email" to user.email,
            "photoUrl" to user.photoUrl,
        )

        firebaseFirestore.collection(USERS_COLLECTION).document(user.uid)
            .set(userDataMap)
            .addOnSuccessListener {
                Log.d("TAG", "User profile saved successfully")
            }
            .addOnFailureListener {
                Log.e("TAG", "Error saving user profile: ${it.message}")
            }
            .await()
    }

    override fun getUserInfoFlow(userId: String): Flow<User?> = callbackFlow {
        val userDocument = firebaseFirestore
            .collection(USERS_COLLECTION)
            .document(userId)

        val listenerRegistration = userDocument.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("TAG", "Error getting user data: ${exception.message}")
                close(exception) // Close the flow with an error
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }

            val userData = snapshot.data
            if (userData == null) {
                Log.e("TAG", "User data is null")
                trySend(null)
                return@addSnapshotListener
            }

            userDocument
                .collection(FRIENDS_COLLECTION)
                .get()
                .addOnSuccessListener { friendsSnapshot ->
                    val friends = friendsSnapshot.documents.map {
                        Friend(
                            uid = it.id,
                            username = it.getString("username") ?: ""
                        )
                    }

                    val user = User(
                        uid = userId,
                        inviteId = userData["inviteId"] as String,
                        username = userData["username"] as String? ?: "User",
                        email = userData["email"] as String?,
                        photoUrl = userData["photoUrl"] as String?,
                        friends = friends
                    )
                    trySend(user)
                }
                .addOnFailureListener {
                    Log.e("TAG", "Error getting friends: ${it.message}")
                }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun getFriendInfoByInviteId(inviteId: String): Friend? =
        findFriendByInviteId(inviteId)

    private suspend fun findFriendByInviteId(inviteId: String): Friend? =
        withContext(Dispatchers.IO) {
            firebaseFirestore
                .collection(USERS_COLLECTION)
                .whereEqualTo("inviteId", inviteId)
                .get()
                .addOnFailureListener {
                    Log.e("TAG", "Error getting user info by inviteId: ${it.message}")
                }
                .await()
                .documents.firstOrNull()
                ?.let {
                    Friend(
                        uid = it.id,
                        username = it.getString("username") ?: "Friend"
                    )
                }
        }

    override suspend fun addFriend(userId: String, inviteId: String): Boolean {
        val friend = findFriendByInviteId(inviteId) ?: run {
            Log.e("TAG", "Friend with inviteId: $inviteId not found")
            return@addFriend false
        }

        val userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId)
        val friendRef = firebaseFirestore.collection(USERS_COLLECTION).document(friend.uid)

        return try {
            firebaseFirestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                if (!userSnapshot.exists()) {
                    Log.e("TAG", "User with userId: $userId not found")
                    return@runTransaction false
                }
                val username = userSnapshot.getString("username") ?: "User"

                val friendSnapshot = transaction.get(friendRef)
                if (!friendSnapshot.exists()) {
                    Log.e("TAG", "Friend with inviteId: $inviteId not found")
                    return@runTransaction false
                }

                val userFriendRef = userRef.collection(FRIENDS_COLLECTION).document(friend.uid)
                val friendFriendRef = friendRef.collection(FRIENDS_COLLECTION).document(userId)

                transaction.set(
                    userFriendRef,
                    mapOf(
                        "addedAt" to System.currentTimeMillis(),
                        "username" to friend.username,
                    )
                )
                transaction.set(
                    friendFriendRef,
                    mapOf(
                        "addedAt" to System.currentTimeMillis(),
                        "username" to username,
                    )
                )
                true
            }.await()
        } catch (exception: Exception) {
            Log.e("TAG", "error adding friend: ${exception.message}", exception)
            false
        }
    }
}