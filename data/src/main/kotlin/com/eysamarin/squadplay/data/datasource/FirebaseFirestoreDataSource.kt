package com.eysamarin.squadplay.data.datasource

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.SecurityLockoutManager
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.EVENTS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.GROUPS_COLLECTION
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource.Companion.USERS_COLLECTION
import com.eysamarin.squadplay.data.entity.EventEntity
import com.eysamarin.squadplay.data.security.isAppCheckAttestationFailure
import com.eysamarin.squadplay.data.toLocalDateTime
import com.eysamarin.squadplay.data.toTimestamp
import com.eysamarin.squadplay.models.AppError
import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.UUID

interface FirebaseFirestoreDataSource {
    fun getUserInfoFlow(userId: String): Flow<User?>
    fun getUserGroupsFlow(userId: String): Flow<List<Group>>
    suspend fun createNewUserGroup(userId: String, title: String): String
    suspend fun getGroupInfo(groupId: String): Group?
    suspend fun joinGroup(userId: String, groupId: String): Boolean
    fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>>
    suspend fun saveUserProfile(user: User)
    @Throws(AppErrorException::class)
    suspend fun isUserProfileExists(userId: String): Boolean
    suspend fun deleteUserProfile(userId: String)
    @Throws(AppErrorException::class)
    suspend fun saveEvent(event: Event): Boolean
    fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>
    suspend fun subscribeToGroupTopic(groupId: String)
    suspend fun unsubscribeFromGroupTopic(groupId: String)
    @Throws(AppErrorException::class)
    suspend fun deleteEvent(eventId: String): Boolean
    @Throws(AppErrorException::class)
    suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus)
    suspend fun renameGroup(groupId: String, newTitle: String): Boolean
    suspend fun deleteGroup(groupId: String): Boolean
    suspend fun leaveGroup(userId: String, groupId: String): Boolean
    suspend fun updateNickname(userId: String, nickname: String): Boolean
    fun clearListeners()

    companion object {
        const val USERS_COLLECTION = "users"
        const val GROUPS_COLLECTION = "groups"
        const val EVENTS_COLLECTION = "events"
    }
}

class FirebaseFirestoreDataSourceImpl(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
    private val logger: AppLogger,
    private val securityLockoutManager: SecurityLockoutManager? = null,
): FirebaseFirestoreDataSource {

    private val activeListeners = Collections.synchronizedSet(mutableSetOf<ListenerRegistration>())

    override fun clearListeners() {
        logger.d(tag = "Firestore") { "Clearing all (${activeListeners.size}) active snapshot listeners" }
        synchronized(activeListeners) {
            activeListeners.forEach { it.remove() }
            activeListeners.clear()
        }
    }

    override suspend fun subscribeToGroupTopic(groupId: String) {
        try {
            firebaseMessaging.subscribeToTopic(groupId).await()
            logger.d(tag = "FCM") { "Subscribed to topic: $groupId" }
        } catch (e: Exception) {
            logger.e(tag = "FCM", throwable = e) { "Error subscribing to topic: $groupId" }
        }
    }

    override suspend fun unsubscribeFromGroupTopic(groupId: String) {
        try {
            firebaseMessaging.unsubscribeFromTopic(groupId).await()
            logger.d(tag = "FCM") { "Unsubscribed from topic: $groupId" }
        } catch (e: Exception) {
            logger.e(tag = "FCM", throwable = e) { "Error unsubscribing from topic: $groupId" }
        }
    }

    @Throws(AppErrorException::class)
    override suspend fun saveEvent(event: Event): Boolean {
        logger.d(tag = "Firestore") { "saveEvent: ${event.uid}" }

        val eventEntity = EventEntity.fromDomain(event)
        val eventDataMap = hashMapOf(
            "creatorId" to eventEntity.creatorId,
            "groupId" to eventEntity.groupId,
            "title" to eventEntity.title,
            "eventIconUrl" to eventEntity.eventIconUrl,
            "dateFrom" to eventEntity.dateFrom,
            "dateTo" to eventEntity.dateTo,
            "responses" to eventEntity.responses,
        )

        val groupsDocumentRef = firebaseFirestore.collection(GROUPS_COLLECTION)
            .document(event.groupId)
        val eventDocumentRef = firebaseFirestore.collection(EVENTS_COLLECTION).document(event.uid)

        return try {
            firebaseFirestore.runTransaction { transaction ->
                val groupDocumentSnapshot = transaction.get(groupsDocumentRef)
                if (!groupDocumentSnapshot.exists()) {
                    logger.e(tag = "Firestore") { "Group with id: ${event.groupId} not found" }
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
            if (exception.isAppCheckAttestationFailure()) {
                logger.w(tag = "Firestore", throwable = exception) { "App Check attestation failed saving event: ${exception.message}" }
                securityLockoutManager?.triggerLockout()
                throw AppErrorException(AppError.SecurityAttestationFailed, cause = exception)
            } else {
                logger.e(tag = "Firestore", throwable = exception) { "Error saving new event: ${exception.message}" }
            }
            false
        }
    }

    @Throws(AppErrorException::class)
    override suspend fun deleteEvent(eventId: String): Boolean = try {
        logger.d(tag = "Firestore") { "Deleting event data for $eventId" }

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

        logger.d(tag = "Firestore") { "Event data deleted successfully for $eventId" }
        true
    } catch (e: Exception) {
        if (e.isAppCheckAttestationFailure()) {
            logger.w(tag = "Firestore", throwable = e) { "App Check attestation failed deleting event: ${e.message}" }
            securityLockoutManager?.triggerLockout()
            throw AppErrorException(AppError.SecurityAttestationFailed, cause = e)
        } else {
            logger.e(tag = "Firestore", throwable = e) { "Error deleting event data for $eventId: ${e.message}" }
        }
        false
    }

    override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> {
        if (groupIds.isEmpty()) {
            logger.d(tag = "Firestore") { "groupIds is empty, returning empty events flow" }
            return flowOf(emptyList())
        }
        if (groupIds.size <= 30) {
            return getEventsFlowForGroupIdsChunk(groupIds.toList())
        }
        val flows = groupIds.chunked(30).map { chunk ->
            getEventsFlowForGroupIdsChunk(chunk)
        }
        return combine(flows) { chunkLists ->
            chunkLists.flatMap { it }
        }
    }

    private fun getEventsFlowForGroupIdsChunk(groupIds: List<String>): Flow<List<Event>> = callbackFlow {
        val eventsCollectionRef = firebaseFirestore.collection(EVENTS_COLLECTION)

        logger.d(tag = "Firestore") { "Subscribe on events flow for groupIds: $groupIds" }
        val listenerRegistration = eventsCollectionRef
            .whereIn("groupId", groupIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.isAppCheckAttestationFailure()) {
                        logger.w(tag = "Firestore", throwable = error) { "App Check attestation failed for events: ${error.message}" }
                        securityLockoutManager?.triggerLockout()
                        close(AppErrorException(AppError.SecurityAttestationFailed, cause = error))
                        return@addSnapshotListener
                    }
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        logger.d(tag = "Firestore") { "Permission denied for events (unauthenticated or unauthorized): ${error.message}" }
                        trySend(emptyList())
                        close()
                        return@addSnapshotListener
                    }
                    logger.e(tag = "Firestore", throwable = error) { "Error getting events: ${error.message}" }
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    logger.w(tag = "Firestore") { "Events snapshot is null or empty for groupIds: $groupIds" }
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot.documents.mapNotNull { document ->
                    val creatorId = document.getString("creatorId") ?: run {
                        logger.e(tag = "Firestore") { "creatorId is null for event: ${document.id}" }
                        return@mapNotNull null
                    }
                    val title = document.getString("title") ?: run {
                        logger.e(tag = "Firestore") { "title is null for event: ${document.id}" }
                        return@mapNotNull null
                    }
                    val groupId = document.getString("groupId") ?: run {
                        logger.e(tag = "Firestore") { "groupId is null for event: ${document.id}" }
                        return@mapNotNull null
                    }
                    val dateFrom = document.getDate("dateFrom") ?: run {
                        logger.e(tag = "Firestore") { "dateFrom is null for event: ${document.id}" }
                        return@mapNotNull null
                    }
                    val dateTo = document.getDate("dateTo") ?: run {
                        logger.e(tag = "Firestore") { "dateTo is null for event: ${document.id}" }
                        return@mapNotNull null
                    }
                    val eventIconUrl = document.getString("eventIconUrl")
                    val responses = (document.get("responses") as? Map<*, *>)
                        ?.entries
                        ?.mapNotNull { (k, v) ->
                            val key = k as? String ?: return@mapNotNull null
                            val value = v as? String ?: return@mapNotNull null
                            key to value
                        }?.toMap() ?: emptyMap()

                    EventEntity(
                        id = document.id,
                        creatorId = creatorId,
                        groupId = groupId,
                        title = title,
                        eventIconUrl = eventIconUrl,
                        dateFrom = com.google.firebase.Timestamp(dateFrom),
                        dateTo = com.google.firebase.Timestamp(dateTo),
                        responses = responses,
                    ).toDomain()
                }
                trySend(events)
            }
        activeListeners.add(listenerRegistration)

        awaitClose {
            logger.d(tag = "Firestore") { "Close getEventsFlow for groupIds: $groupIds" }
            activeListeners.remove(listenerRegistration)
            listenerRegistration.remove()
        }
    }

    @Throws(AppErrorException::class)
    override suspend fun updateEventResponse(
        eventId: String,
        userId: String,
        status: EventResponseStatus,
    ) {
        logger.d(tag = "Firestore") { "updateEventResponse: eventId=$eventId, userId=$userId, status=$status" }
        val statusString = when (status) {
            EventResponseStatus.ACCEPTED -> "ACCEPTED"
            EventResponseStatus.REJECTED -> "REJECTED"
            EventResponseStatus.NOT_SET -> null
        }
        val updates = mapOf("responses.$userId" to statusString)
        try {
            firebaseFirestore.collection(EVENTS_COLLECTION).document(eventId).update(updates).await()
        } catch (e: Exception) {
            if (e.isAppCheckAttestationFailure()) {
                logger.w(tag = "Firestore", throwable = e) { "App Check attestation failed in updateEventResponse: ${e.message}" }
                securityLockoutManager?.triggerLockout()
                throw AppErrorException(AppError.SecurityAttestationFailed, cause = e)
            }
            throw e
        }
    }

    override suspend fun deleteUserProfile(userId: String) {
        logger.d(tag = "Firestore") { "Deleting user data for $userId" }
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

            logger.d(tag = "Firestore") { "User data deleted successfully for $userId" }
        } catch (e: Exception) {
            logger.e(tag = "Firestore", throwable = e) { "Error deleting user data for $userId: ${e.message}" }
        }
    }

    private suspend fun getCollectionDocuments(
        collectionRef: CollectionReference,
    ): List<DocumentSnapshot> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = collectionRef.get().await()
            querySnapshot.documents
        } catch (e: Exception) {
            logger.e(tag = "Firestore", throwable = e) { "Error getting documents: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun saveUserProfile(user: User) {
        val userDataMap = hashMapOf<String, Any?>(
            "uid" to user.uid,
            "username" to user.username,
            "email" to user.email,
            "photoUrl" to user.photoUrl,
        )
        if (user.nickname != null) {
            userDataMap["nickname"] = user.nickname
        }

        firebaseFirestore.collection(USERS_COLLECTION).document(user.uid)
            .set(userDataMap, SetOptions.merge())
            .addOnSuccessListener {
                logger.d(tag = "Firestore") { "User profile saved successfully" }
            }
            .addOnFailureListener {
                logger.e(tag = "Firestore", throwable = it) { "Error saving user profile: ${it.message}" }
            }
            .await()
    }

    @Throws(AppErrorException::class)
    override suspend fun isUserProfileExists(userId: String): Boolean = try {
        val userDocumentSnapshot = firebaseFirestore.collection(USERS_COLLECTION)
            .document(userId).get().await()
        userDocumentSnapshot.exists()
    } catch (e: FirebaseFirestoreException) {
        if (e.isAppCheckAttestationFailure()) {
            logger.w(tag = "Firestore", throwable = e) { "App Check attestation failed in isUserProfileExists: ${e.message}" }
            securityLockoutManager?.triggerLockout()
            throw AppErrorException(AppError.SecurityAttestationFailed, cause = e)
        }
        false
    }

    override fun getUserInfoFlow(userId: String): Flow<User?> = callbackFlow {
        val userDocument = firebaseFirestore
            .collection(USERS_COLLECTION)
            .document(userId)

        logger.d(tag = "Firestore") { "Subscribe on user info flow for userId: $userId" }
        val listenerRegistration = userDocument.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                if (exception.isAppCheckAttestationFailure()) {
                    logger.w(tag = "Firestore", throwable = exception) { "App Check attestation failed for user data: ${exception.message}" }
                    securityLockoutManager?.triggerLockout()
                    close(AppErrorException(AppError.SecurityAttestationFailed, cause = exception))
                    return@addSnapshotListener
                }
                if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    logger.d(tag = "Firestore") { "Permission denied for user data (unauthenticated or unauthorized): ${exception.message}" }
                    trySend(null)
                    close()
                    return@addSnapshotListener
                }
                logger.e(tag = "Firestore", throwable = exception) { "Error getting user data: ${exception.message}" }
                close(exception)
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }

            val userData = snapshot.data
            if (userData == null) {
                logger.e(tag = "Firestore") { "User data is null for userId: $userId" }
                trySend(null)
                return@addSnapshotListener
            }

            val user = User(
                uid = userId,
                username = userData["username"] as String? ?: "User",
                email = userData["email"] as String?,
                photoUrl = userData["photoUrl"] as String?,
                groups = emptyList(),
                nickname = userData["nickname"] as String?,
            )
            trySend(user)
        }
        activeListeners.add(listenerRegistration)

        awaitClose {
            logger.d(tag = "Firestore") { "Close getUserInfoFlow for userId: $userId" }
            activeListeners.remove(listenerRegistration)
            listenerRegistration.remove()
        }
    }

    override fun getUserGroupsFlow(userId: String): Flow<List<Group>> = callbackFlow {
        val groupsCollectionRef = firebaseFirestore.collection(GROUPS_COLLECTION)

        logger.d(tag = "Firestore") { "Subscribe on user groups flow for userId: $userId" }
        val listenerRegistration = groupsCollectionRef
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.isAppCheckAttestationFailure()) {
                        logger.w(tag = "Firestore", throwable = error) { "App Check attestation failed for groups: ${error.message}" }
                        securityLockoutManager?.triggerLockout()
                        close(AppErrorException(AppError.SecurityAttestationFailed, cause = error))
                        return@addSnapshotListener
                    }
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        logger.d(tag = "Firestore") { "Permission denied for groups (unauthenticated or unauthorized): ${error.message}" }
                        trySend(emptyList())
                        close()
                        return@addSnapshotListener
                    }
                    logger.e(tag = "Firestore", throwable = error) { "Error getting groups: ${error.message}" }
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    logger.w(tag = "Firestore") { "Groups snapshot is null or empty for userId: $userId" }
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
                        } ?: emptyList(),
                        ownerId = document.getString("ownerId")
                    )
                }
                trySend(groups)
            }
        activeListeners.add(listenerRegistration)

        awaitClose {
            logger.d(tag = "Firestore") { "Close getUserGroupsFlow for userId: $userId" }
            activeListeners.remove(listenerRegistration)
            listenerRegistration.remove()
        }
    }

    override fun getGroupsMembersInfoFlow(
        groups: List<Group>,
    ): Flow<List<UserGroupSection>> = callbackFlow {
        if (groups.isEmpty()) {
            logger.d(tag = "Firestore") { "Groups list is empty" }
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val members = groups.flatMap { group -> group.members }.distinct()

        if (members.isEmpty()) {
            logger.d(tag = "Firestore") { "Members list is empty" }
            val emptySections = groups.map { group ->
                UserGroupSection(
                    groupId = group.uid,
                    title = group.title,
                    members = emptyList(),
                    ownerId = group.ownerId,
                )
            }
            trySend(emptySections)
            close()
            return@callbackFlow
        }

        val friendsQuery = firebaseFirestore.collection(USERS_COLLECTION).whereIn("uid", members.take(30))
        logger.d(tag = "Firestore") { "Subscribe on user friends flow" }
        val listenerRegistration = friendsQuery.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                if (exception.isAppCheckAttestationFailure()) {
                    logger.w(tag = "Firestore", throwable = exception) { "App Check attestation failed for groups members info: ${exception.message}" }
                    securityLockoutManager?.triggerLockout()
                    close(AppErrorException(AppError.SecurityAttestationFailed, cause = exception))
                    return@addSnapshotListener
                }
                if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    logger.d(tag = "Firestore") { "Permission denied for groups members info (unauthenticated or unauthorized): ${exception.message}" }
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }
                logger.e(tag = "Firestore", throwable = exception) { "Error getting user data: ${exception.message}" }
                close(exception)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                logger.d(tag = "Firestore") { "Friends snapshot is null or empty" }
                val emptySections = groups.map { group ->
                    UserGroupSection(
                        groupId = group.uid,
                        title = group.title,
                        members = emptyList(),
                        ownerId = group.ownerId,
                    )
                }
                trySend(emptySections)
                return@addSnapshotListener
            }

            val membersById = snapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                val uid = data["uid"] as? String ?: document.id
                val username = data["username"] as? String ?: "User"
                val photoUrl = data["photoUrl"] as? String
                val nickname = data["nickname"] as? String
                uid to Triple(username, photoUrl, nickname)
            }.toMap()

            val sections = groups.map { group ->
                UserGroupSection(
                    groupId = group.uid,
                    title = group.title,
                    members = group.members.mapNotNull { memberUid ->
                        val memberData = membersById[memberUid] ?: return@mapNotNull null
                        Friend(
                            uid = memberUid,
                            username = memberData.first,
                            photoUrl = memberData.second,
                            groupTitleFrom = group.title,
                            nickname = memberData.third,
                        )
                    },
                    ownerId = group.ownerId,
                )
            }

            trySend(sections)
        }
        activeListeners.add(listenerRegistration)

        awaitClose {
            logger.d(tag = "Firestore") { "Close getGroupsMembersInfoFlow" }
            activeListeners.remove(listenerRegistration)
            listenerRegistration.remove()
        }
    }

    override suspend fun createNewUserGroup(userId: String, title: String): String {
        val newGroupUid = UUID.randomUUID().toString()

        val groupDataMap = hashMapOf(
            "members" to listOf(userId),
            "title" to title,
            "ownerId" to userId,
        )

        firebaseFirestore.collection(GROUPS_COLLECTION).document(newGroupUid)
            .set(groupDataMap)
            .addOnSuccessListener {
                logger.d(tag = "Firestore") { "Group created successfully with uid: $newGroupUid" }
            }
            .addOnFailureListener {
                logger.e(tag = "Firestore", throwable = it) { "Error creating new user group: ${it.message}" }
            }
            .await()

        return newGroupUid
    }

    override suspend fun getGroupInfo(groupId: String): Group? = withContext(Dispatchers.IO) {
        val groupDocumentSnapshot = firebaseFirestore.collection(GROUPS_COLLECTION)
            .document(groupId).get().await()

        if (!groupDocumentSnapshot.exists()) {
            logger.w(tag = "Firestore") { "Group with id: $groupId not found" }
            return@withContext null
        }

        val members = groupDocumentSnapshot["members"]?.let {
            val anyList = it as? List<*>
            anyList?.filterIsInstance<String>()
        } ?: emptyList()

        return@withContext Group(
            uid = groupId,
            title = groupDocumentSnapshot.getString("title") ?: "",
            members = members,
            ownerId = groupDocumentSnapshot.getString("ownerId")
        )
    }

    override suspend fun joinGroup(userId: String, groupId: String): Boolean {
        val groupRef = firebaseFirestore.collection(GROUPS_COLLECTION).document(groupId)

        return try {
            firebaseFirestore.runTransaction { transaction ->
                val groupDocumentSnapshot = transaction.get(groupRef)
                if (!groupDocumentSnapshot.exists()) {
                    logger.e(tag = "Firestore") { "Group with id: $groupId not found" }
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
            logger.e(tag = "Firestore", throwable = exception) { "Error joining group: ${exception.message}" }
            false
        }
    }

    override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = try {
        logger.d(tag = "Firestore") { "Renaming group $groupId to $newTitle" }
        firebaseFirestore.collection(GROUPS_COLLECTION).document(groupId)
            .update("title", newTitle)
            .await()
        logger.d(tag = "Firestore") { "Group $groupId renamed successfully to $newTitle" }
        true
    } catch (e: Exception) {
        logger.e(tag = "Firestore", throwable = e) { "Error renaming group $groupId: ${e.message}" }
        false
    }

    override suspend fun deleteGroup(groupId: String): Boolean = try {
        logger.d(tag = "Firestore") { "Deleting group $groupId" }
        unsubscribeFromGroupTopic(groupId)

        val eventsSnapshot = firebaseFirestore.collection(EVENTS_COLLECTION)
            .whereEqualTo("groupId", groupId)
            .get()
            .await()

        val groupDocRef = firebaseFirestore.collection(GROUPS_COLLECTION).document(groupId)
        val groupSnapshot = groupDocRef.get().await()
        val eventIdsFromGroup = groupSnapshot["events"]?.let {
            val anyList = it as? List<*>
            anyList?.filterIsInstance<String>()
        } ?: emptyList()

        val eventDocRefs = mutableSetOf<DocumentReference>()
        for (doc in eventsSnapshot.documents) {
            eventDocRefs.add(doc.reference)
        }
        for (eventId in eventIdsFromGroup) {
            eventDocRefs.add(firebaseFirestore.collection(EVENTS_COLLECTION).document(eventId))
        }

        val allRefsToDelete = eventDocRefs + groupDocRef
        allRefsToDelete.chunked(500).forEach { chunk ->
            val batch = firebaseFirestore.batch()
            chunk.forEach { ref -> batch.delete(ref) }
            batch.commit().await()
        }

        logger.d(tag = "Firestore") { "Group $groupId and ${eventDocRefs.size} associated events deleted successfully" }
        true
    } catch (e: Exception) {
        logger.e(tag = "Firestore", throwable = e) { "Error deleting group $groupId: ${e.message}" }
        false
    }

    override suspend fun leaveGroup(userId: String, groupId: String): Boolean {
        val groupRef = firebaseFirestore.collection(GROUPS_COLLECTION).document(groupId)

        return try {
            logger.d(tag = "Firestore") { "User $userId leaving group $groupId" }
            unsubscribeFromGroupTopic(groupId)

            val groupDocumentSnapshot = groupRef.get().await()
            if (!groupDocumentSnapshot.exists()) {
                logger.e(tag = "Firestore") { "Group with id: $groupId not found" }
                return false
            }

            val members = groupDocumentSnapshot["members"]?.let {
                val anyList = it as? List<*>
                anyList?.filterIsInstance<String>()
            } ?: emptyList()

            val eventsSnapshot = firebaseFirestore.collection(EVENTS_COLLECTION)
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            val eventsToUpdate = eventsSnapshot.documents.filter { doc ->
                val responses = doc.get("responses") as? Map<*, *>
                responses?.containsKey(userId) == true
            }

            val batchOperations = mutableListOf<(com.google.firebase.firestore.WriteBatch) -> Unit>()
            batchOperations.add { batch ->
                batch.update(groupRef, mapOf("members" to members.minus(userId)))
            }
            for (eventDoc in eventsToUpdate) {
                batchOperations.add { batch ->
                    batch.update(eventDoc.reference, FieldPath.of("responses", userId), FieldValue.delete())
                }
            }

            batchOperations.chunked(500).forEach { chunk ->
                val batch = firebaseFirestore.batch()
                chunk.forEach { operation -> operation(batch) }
                batch.commit().await()
            }

            logger.d(tag = "Firestore") {
                "User $userId successfully left group $groupId and responses removed from ${eventsToUpdate.size} events"
            }
            true
        } catch (exception: Exception) {
            logger.e(tag = "Firestore", throwable = exception) { "Error leaving group: ${exception.message}" }
            false
        }
    }

    override suspend fun updateNickname(userId: String, nickname: String): Boolean = try {
        logger.d(tag = "Firestore") { "Updating nickname for $userId to $nickname" }
        firebaseFirestore.collection(USERS_COLLECTION).document(userId)
            .set(mapOf("nickname" to nickname), SetOptions.merge())
            .await()
        logger.d(tag = "Firestore") { "Nickname updated successfully for $userId" }
        true
    } catch (e: Exception) {
        logger.e(tag = "Firestore", throwable = e) { "Error updating nickname for $userId: ${e.message}" }
        false
    }
}