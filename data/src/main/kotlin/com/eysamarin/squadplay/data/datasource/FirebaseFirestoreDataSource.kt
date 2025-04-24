package com.eysamarin.squadplay.data.datasource

import android.util.Log
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.EVENTS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.GROUPS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.USERS_COLLECTION
import com.eysamarin.squadplay.data.toLocalDateTime
import com.eysamarin.squadplay.data.toTimestamp
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.FirebaseMessaging
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
    suspend fun isUserProfileExists(userId: String): Boolean
    suspend fun deleteUserProfile(userId: String)
    suspend fun saveEvent(event: Event): Boolean
    fun getEventsFlow(groupId: String): Flow<List<Event>>
    suspend fun subscribeToGroupTopic(groupId: String)
    suspend fun deleteEvent(eventId: String): Boolean

    companion object {
        const val USERS_COLLECTION = "users"
        const val GROUPS_COLLECTION = "groups"
        const val EVENTS_COLLECTION = "events"
    }
}

class FirebaseFirestoreDataSourceImpl(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
): FirebaseFirestoreDataSource {

    override suspend fun subscribeToGroupTopic(groupId: String) {
        try {
            firebaseMessaging.subscribeToTopic(groupId).await()
            Log.d("FCM", "Subscribed to topic: $groupId")
        } catch (e: Exception) {
            Log.e("FCM", "Error subscribing to topic: $groupId", e)
        }
    }

    private suspend fun unsubscribeFromGroupTopic(groupId: String) {
        try {
            firebaseMessaging.unsubscribeFromTopic(groupId).await()
            Log.d("FCM", "Unsubscribed from topic: $groupId")
        } catch (e: Exception) {
            Log.e("FCM", "Error unsubscribing from topic: $groupId", e)
        }
    }

    override suspend fun saveEvent(event: Event): Boolean {
        Log.d("TAG", "saveEvent: $event")

        val eventDataMap = hashMapOf(
            "creatorId" to event.creatorId,
            "groupId" to event.groupId,
            "title" to event.title,
            "dateFrom" to event.fromDateTime.toTimestamp(),
            "dateTo" to event.toDateTime.toTimestamp(),
        )

        val groupsDocumentRef = firebaseFirestore.collection(GROUPS_COLLECTION)
            .document(event.groupId)
        val eventDocumentRef = firebaseFirestore.collection(EVENTS_COLLECTION).document(event.uid)

        return try {
            firebaseFirestore.runTransaction { transaction ->
                val groupDocumentSnapshot = transaction.get(groupsDocumentRef)
                if (!groupDocumentSnapshot.exists()) {
                    Log.e("TAG", "Group with id: ${event.groupId} not found")
                    return@runTransaction false
                }
                val events = groupDocumentSnapshot["events"]?.let {
                    val anyList = it as? List<*>
                    anyList?.filterIsInstance<String>()
                } ?: emptyList()

                transaction.set(eventDocumentRef, eventDataMap)
                transaction.update(groupsDocumentRef, mapOf("events" to events.plus(event.uid)))
                true
            }.await()
        } catch (exception: Exception) {
            Log.e("TAG", "error saving new event: ${exception.message}", exception)
            false
        }
    }

    override suspend fun deleteEvent(eventId: String): Boolean = try {
        Log.d("TAG", "Deleting event data for $eventId")

        val eventDocumentRef = firebaseFirestore.collection(EVENTS_COLLECTION).document(eventId)
        val groupsCollectionRef = firebaseFirestore.collection(GROUPS_COLLECTION)

        firebaseFirestore.runTransaction { transaction ->
            val eventDocumentSnapshot = transaction.get(eventDocumentRef)

            val relatedGroupId = eventDocumentSnapshot.getString("groupId")
                ?: return@runTransaction false
            val groupDocumentRef = groupsCollectionRef.document(relatedGroupId)
            val groupDocumentSnapshot = transaction.get(groupDocumentRef)

            val groupEvents = groupDocumentSnapshot["events"]?.let {
                val anyList = it as? List<*>
                anyList?.filterIsInstance<String>()
            } ?: emptyList()

            transaction.update(groupDocumentRef, mapOf("events" to groupEvents.minus(eventId)))
            transaction.delete(eventDocumentRef)
        }.await()

        Log.d("TAG", "Event data deleted successfully for $eventId")
        true
    } catch (e: Exception) {
        println("Error deleting event data for $eventId: ${e.message}")
        false
    }

    override fun getEventsFlow(groupId: String): Flow<List<Event>> = callbackFlow {
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

                    Event(
                        uid = document.id,
                        creatorId = creatorId,
                        groupId = groupId,
                        title = title,
                        fromDateTime = dateFrom.toLocalDateTime(),
                        toDateTime = dateTo.toLocalDateTime(),
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
            val userDocumentRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId)
            val groupsCollectionRef = firebaseFirestore.collection(USERS_COLLECTION)
            val groupsDocuments = getCollectionDocuments(groupsCollectionRef)
                .also { it.forEach { unsubscribeFromGroupTopic(it.id) } }

            firebaseFirestore.runTransaction { transaction ->
                groupsDocuments.forEach {
                    if (!it.exists()) return@forEach

                    val members = it["members"]?.let {
                        val anyList = it as? List<*>
                        anyList?.filterIsInstance<String>()
                    } ?: emptyList()
                    transaction.update(it.reference, mapOf("members" to members.minus(userId)))
                }
                transaction.delete(userDocumentRef)
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

    override suspend fun isUserProfileExists(userId: String): Boolean = try {
        val userDocumentSnapshot = firebaseFirestore.collection(USERS_COLLECTION)
            .document(userId).get().await()
        userDocumentSnapshot.exists()
    } catch (_: FirebaseFirestoreException) {
        false
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

        if (members.isEmpty()) {
            Log.d("TAG", "Members list is empty")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

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