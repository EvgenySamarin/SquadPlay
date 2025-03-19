package com.eysamarin.squadplay.data.datasource

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Deprecated("migrated to firestore")
interface FirebaseDatabaseDataSource {
    fun saveTestData(string: String)
}

class FirebaseDatabaseDataSourceImpl(
    private val firebaseDatabase: FirebaseDatabase,
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
}