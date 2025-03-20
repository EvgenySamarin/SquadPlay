package com.eysamarin.squadplay.data.datasource

import android.util.Log
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.EVENTS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.GROUPS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.USERS_COLLECTION
import com.eysamarin.squadplay.models.EventData
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
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
import java.util.UUID

interface FirebaseFirestoreDataSource {
    fun getUserInfoFlow(userId: String): Flow<User?>
    fun getUserGroupsFlow(userId: String): Flow<List<Group>>
    suspend fun createNewUserGroup(userId: String, title: String): String
    suspend fun getGroupInfo(groupId: String): Group?
    suspend fun joinGroup(userId: String, groupId: String): Boolean
    fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<Friend>>
    suspend fun saveUserProfile(user: User)
    suspend fun deleteUserProfile(userId: String)
    suspend fun saveEvent(event: EventData)
    fun getEventsFlow(groupId: String): Flow<List<EventData>>

    companion object {
        const val USERS_COLLECTION = "users"
        const val GROUPS_COLLECTION = "groups"
        const val EVENTS_COLLECTION = "events"
    }
}

class FirebaseFirestoreDataSourceImpl(
    private val firebaseFirestore: FirebaseFirestore,
): FirebaseFirestoreDataSource {

    override suspend fun saveEvent(event: EventData) {
        Log.d("TAG", "saveEvent: $event")

        val eventId = UUID.randomUUID().toString()
        val eventDataMap = hashMapOf(
            "creatorId" to event.creatorId,
            "groupId" to event.groupId,
            "title" to event.title,
            "dateFrom" to event.fromDateTime,
            "dateTo" to event.toDateTime,
        )

        firebaseFirestore.collection(EVENTS_COLLECTION).document(eventId)
            .set(eventDataMap)
            .addOnSuccessListener {
                Log.d("TAG", "Event saved successfully with id: $eventId")
            }
            .addOnFailureListener {
                Log.e("TAG", "Error saving event: ${it.message}")
            }
            .await()
    }

    override fun getEventsFlow(groupId: String): Flow<List<EventData>> = callbackFlow {
        val eventsCollectionRef = firebaseFirestore.collection(EVENTS_COLLECTION)

        Log.d("TAG", "subscribe on events flow")
        val listenerRegistration = eventsCollectionRef
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TAG", "Error getting events: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.w("TAG", "Events snapshot is null or empty")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot.documents.mapNotNull { document ->
                    val creatorId = document.getString("creatorId") ?: run {
                        Log.e("TAG", "creatorId is null for event: ${document.id}")
                        return@mapNotNull null
                    }
                    val title = document.getString("title") ?: run {
                        Log.e("TAG", "title is null for event: ${document.id}")
                        return@mapNotNull null
                    }
                    val groupId = document.getString("groupId") ?: run {
                        Log.e("TAG", "groupId is null for event: ${document.id}")
                        return@mapNotNull null
                    }
                    val dateFrom = document.getDate("dateFrom") ?: run {
                        Log.e("TAG", "dateFrom is null for event: ${document.id}")
                        return@mapNotNull null
                    }
                    val dateTo = document.getDate("dateTo") ?: run {
                        Log.e("TAG", "dateTo is null for event: ${document.id}")
                        return@mapNotNull null
                    }

                    EventData(
                        creatorId = creatorId,
                        groupId = groupId,
                        title = title,
                        fromDateTime = dateFrom.toInstant().atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime(),
                        toDateTime = dateTo.toInstant().atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime(),
                    )
                }
                trySend(events)
            }

        awaitClose {
            Log.d("TAG", "close getEventsFlow")
            listenerRegistration.remove()
        }
    }

    override suspend fun deleteUserProfile(userId: String) {
        Log.d("TAG", "Deleting user data for $userId")
        try {
            val userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId)
            val groupsRef = userRef.collection(GROUPS_COLLECTION)
            val groupDocs = getCollectionDocuments(groupsRef)

            firebaseFirestore.runTransaction { transaction ->
                groupDocs.forEach {
                    val members = it["members"]?.let {
                        val anyList = it as? List<*>
                        anyList?.filterIsInstance<String>()
                    } ?: emptyList()
                    transaction.update(it.reference, mapOf("members" to members.minus(userId)))
                }
                transaction.delete(userRef)
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
            "uid" to user.uid,
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

        Log.d("TAG", "subscribe on user info flow")
        val listenerRegistration = userDocument.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("TAG", "Error getting user data: ${exception.message}")
                close(exception)
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

            val user = User(
                uid = userId,
                username = userData["username"] as String? ?: "User",
                email = userData["email"] as String?,
                photoUrl = userData["photoUrl"] as String?,
                groups = emptyList()
            )
            trySend(user)
        }

        awaitClose {
            Log.d("TAG", "close getUserInfoFlow")
            listenerRegistration.remove()
        }
    }

    override fun getUserGroupsFlow(userId: String): Flow<List<Group>> = callbackFlow {
        val groupsCollectionRef = firebaseFirestore.collection(GROUPS_COLLECTION)

        Log.d("TAG", "subscribe on user groups flow")
        val listenerRegistration = groupsCollectionRef
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TAG", "Error getting groups: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.w("TAG", "Groups snapshot is null or empty")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val groups = snapshot.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: return@mapNotNull null
                    Group(
                        uid = document.id,
                        title = title,
                        members = document["members"]?.let {
                            val anyList = it as? List<*>
                            anyList?.filterIsInstance<String>()
                        } ?: emptyList()
                    )
                }
                trySend(groups)
            }

        awaitClose {
            Log.d("TAG", "close getUserGroupsFlow")
            listenerRegistration.remove()
        }
    }

    override fun getGroupsMembersInfoFlow(
        groups: List<Group>,
    ): Flow<List<Friend>> = callbackFlow {
        val members = groups.map { group -> group.members }.flatten().distinct()

        val friendsQuery = firebaseFirestore.collection(USERS_COLLECTION).whereIn("uid", members)
        Log.d("TAG", "subscribe on user friends flow")
        val listenerRegistration = friendsQuery.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("TAG", "Error getting user data: ${exception.message}")
                close(exception)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                Log.d("TAG", "Friends snapshot is null or empty")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val friends = snapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null

                Friend(
                    uid = data["uid"] as? String ?: document.id,
                    username = data["username"] as? String ?: "User",
                    photoUrl = data["photoUrl"] as? String,
                    groupTitleFrom = groups
                        .find { it.members.contains(data["uid"] as? String) }
                        ?.title ?: ""
                )
            }

            trySend(friends)
        }

        awaitClose {
            Log.d("TAG", "close getGroupsMembersInfoFlow")
            listenerRegistration.remove()
        }
    }

    override suspend fun createNewUserGroup(userId: String, title: String): String {
        val newGroupUid = UUID.randomUUID().toString()

        val groupDataMap = hashMapOf(
            "members" to listOf(userId),
            "title" to title,
        )

        firebaseFirestore.collection(GROUPS_COLLECTION).document(newGroupUid)
            .set(groupDataMap)
            .addOnSuccessListener {
                Log.d("TAG", "Group created successfully with uid: $newGroupUid")
            }
            .addOnFailureListener {
                Log.e("TAG", "Error creating new user group: ${it.message}")
            }
            .await()

        return newGroupUid
    }

    override suspend fun getGroupInfo(groupId: String): Group? = withContext(Dispatchers.IO) {
        val groupDocumentSnapshot = firebaseFirestore.collection(GROUPS_COLLECTION)
            .document(groupId).get().await()

        if (!groupDocumentSnapshot.exists()) {
            Log.w("TAG", "Group with id: $groupId not found")
            return@withContext null
        }

        val members = groupDocumentSnapshot["members"]?.let {
            val anyList = it as? List<*>
            anyList?.filterIsInstance<String>()
        } ?: emptyList()

        return@withContext Group(
            uid = groupId,
            title = groupDocumentSnapshot.getString("title") ?: "",
            members = members
        )
    }

    override suspend fun joinGroup(userId: String, groupId: String): Boolean {
        val groupRef = firebaseFirestore.collection(GROUPS_COLLECTION).document(groupId)

        return try {
            firebaseFirestore.runTransaction { transaction ->
                val groupDocumentSnapshot = transaction.get(groupRef)
                if (!groupDocumentSnapshot.exists()) {
                    Log.e("TAG", "Group with id: $groupId not found")
                    return@runTransaction false
                }
                val members = groupDocumentSnapshot["members"]?.let {
                    val anyList = it as? List<*>
                    anyList?.filterIsInstance<String>()
                } ?: emptyList()

                transaction.update(groupRef, mapOf("members" to members.plus(userId)))
                true
            }.await()
        } catch (exception: Exception) {
            Log.e("TAG", "error joining group: ${exception.message}", exception)
            false
        }
    }
}