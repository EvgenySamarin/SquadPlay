package com.eysamarin.squadplay.data.datasource

import android.util.Log
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSource.Companion.FRIENDS_DATASET
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSource.Companion.USERS_DATASET
import com.eysamarin.squadplay.models.Friend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface FirebaseDatabaseDataSource {
    fun saveTestData(string: String)
    suspend fun getFriends(userId: String): List<Friend>
    fun addFriend(userId: String, friendId: String)

    companion object {
        const val USERS_DATASET = "users"
        const val FRIENDS_DATASET = "friends"
    }
}

class FirebaseDatabaseDataSourceImpl(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseFirestore: FirebaseFirestore,
): FirebaseDatabaseDataSource {

    val testsDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val data = snapshot.getValue(String::class.java)
                if (data != null) {
                    Log.d("Firebase", "Data retrieved: $data")
                }
            } else {
                Log.d("Firebase", "No data found.")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "Failed to read value.", error.toException())
        }
    }

    override fun saveTestData(data: String) {
        val testRef = firebaseDatabase.getReference("tests").also {
            it.addValueEventListener(testsDataListener)
        }

        testRef.setValue(data)
            .addOnSuccessListener {
                Log.d("Firebase", "test data: $data added successfully.")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error writing test data: $data", exception)
            }
    }

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