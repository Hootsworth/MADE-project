package com.afterlight.madeproject.data.repository

import com.afterlight.madeproject.domain.model.DepartmentScore
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.EventStatus
import com.afterlight.madeproject.domain.model.RecapPost
import com.afterlight.madeproject.domain.model.RSVPActivity
import com.afterlight.madeproject.domain.model.Promotion
import com.afterlight.madeproject.domain.model.VibeTag
import com.afterlight.madeproject.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EventRepository {

    override fun observeFeaturedAndFeed(interests: List<String>): Flow<List<Event>> = callbackFlow {
        // Removed whereEqualTo("status", "live") to bypass the need for a composite index
        val query = firestore.collection("events")
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snap, error ->
            if (error != null) {
                // If it fails, fallback to simple fetch without order
                firestore.collection("events").whereEqualTo("status", "live").get()
                    .addOnSuccessListener { backupSnap ->
                        val backupEvents = backupSnap.documents.map { it.toEvent() }.sortedBy { it.dateTime }
                        trySend(backupEvents)
                    }
                return@addSnapshotListener
            }
            
            val events = snap?.documents.orEmpty()
                .map { doc -> doc.toEvent() }
                .filter { it.status == EventStatus.LIVE }

            val personalized = if (interests.isEmpty()) {
                events
            } else {
                events.sortedByDescending { event ->
                    event.tags.count { interests.contains(it) } + event.vibes.count { vibe ->
                        interests.contains(vibe.name.lowercase())
                    }
                }
            }
            trySend(personalized)
        }
        awaitClose { listener.remove() }
    }

    override fun observeHappeningThisWeek(): Flow<List<Event>> = callbackFlow {
        val now = System.currentTimeMillis()
        val week = now + 7 * 24 * 60 * 60 * 1000L
        
        // Removed whereEqualTo("status", "live") to bypass composite index
        val listener = firestore.collection("events")
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .startAt(now)
            .endAt(week)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    firestore.collection("events").whereEqualTo("status", "live").get()
                        .addOnSuccessListener { backupSnap ->
                            val backupEvents = backupSnap.documents.map { it.toEvent() }
                                .filter { it.dateTime in now..week }
                                .sortedBy { it.dateTime }
                            trySend(backupEvents)
                        }
                    return@addSnapshotListener
                }
                
                val filtered = snap?.documents.orEmpty()
                    .map { it.toEvent() }
                    .filter { it.status == EventStatus.LIVE }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    override fun observeSocialProofTicker(eventId: String): Flow<String> = callbackFlow {
        val listener = firestore.collection("rsvps").document(eventId)
            .collection("attendees")
            .orderBy("rsvpAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snap, _ ->
                val top = snap?.documents?.firstOrNull()
                if (top == null) {
                    trySend("Be the first to RSVP")
                } else {
                    val name = top.getString("name").orEmpty().ifBlank { "Someone" }
                    val minutes = ((System.currentTimeMillis() - (top.getLong("rsvpAt") ?: 0L)) / 60000L)
                        .coerceAtLeast(1L)
                    trySend("$name just signed up · $minutes min ago")
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeLeaderboard(): Flow<List<DepartmentScore>> = callbackFlow {
        val listener = firestore.collection("departments")
            .orderBy("monthlyScore", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snap, _ ->
                val departments = snap?.documents.orEmpty().map { doc ->
                    DepartmentScore(
                        departmentId = doc.id,
                        name = doc.getString("name").orEmpty(),
                        monthlyScore = (doc.getLong("monthlyScore") ?: 0L).toInt(),
                        eventCount = (doc.getLong("eventCount") ?: 0L).toInt(),
                        attendeeCount = (doc.getLong("attendeeCount") ?: 0L).toInt()
                    )
                }
                trySend(departments)
            }
        awaitClose { listener.remove() }
    }

    override fun searchEvents(
        query: String,
        category: String?,
        department: String?,
        dateStart: Long?,
        dateEnd: Long?,
        freeOnly: Boolean,
        minCapacity: Int?,
        vibes: List<String>
    ): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("status", "live")
            .addSnapshotListener { snap, _ ->
                val filtered = snap?.documents.orEmpty().map { it.toEvent() }
                    .filter { event -> query.isBlank() || event.title.contains(query, true) }
                    .filter { event -> category.isNullOrBlank() || category.equals(event.category, true) }
                    .filter { event -> department.isNullOrBlank() || event.tags.any { it.equals(department, true) } }
                    .filter { event -> !freeOnly || !event.isPaid }
                    .filter { event -> minCapacity == null || event.capacity >= minCapacity }
                    .filter { event -> vibes.isEmpty() || event.vibes.any { v -> vibes.contains(v.name.lowercase()) } }
                    .filter { event -> dateStart == null || event.dateTime >= dateStart }
                    .filter { event -> dateEnd == null || event.dateTime <= dateEnd }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun rsvp(eventId: String, referredBy: String?): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        val userSnapshot = firestore.collection("users").document(uid).get().await()
        val profileName = userSnapshot.getString("name")
        val profileEmail = userSnapshot.getString("email")
        val attendeeRef = firestore.collection("rsvps").document(eventId)
            .collection("attendees").document(uid)
        val ticketId = UUID.randomUUID().toString()
        val attendee = mapOf(
            "uid" to uid,
            "name" to (profileName ?: auth.currentUser?.displayName ?: "Student"),
            "email" to (profileEmail ?: auth.currentUser?.email ?: ""),
            "rsvpAt" to System.currentTimeMillis(),
            "checkInStatus" to false,
            "ticketId" to ticketId,
            "referredBy" to referredBy
        )
        val eventRef = firestore.collection("events").document(eventId)
        try {
            val eventSnapshot = eventRef.get().await()
            val eventStatus = eventSnapshot.getString("status").orEmpty()
            val eventTime = eventSnapshot.getLong("dateTime") ?: 0L
            val capacity = (eventSnapshot.getLong("capacity") ?: 0L).toInt()
            val currentRsvp = (eventSnapshot.getLong("rsvpCount") ?: 0L).toInt()

            if (eventStatus == "past" || eventTime <= System.currentTimeMillis()) {
                error("RSVP is closed for this event.")
            }

            // If already signed up
            val existing = attendeeRef.get().await()
            if (existing.exists()) error("You have already RSVP'd to this event.")

            // If capacity full, add to waitlist instead of failing hard
            if (capacity > 0 && currentRsvp >= capacity) {
                val waitRef = firestore.collection("rsvps").document(eventId)
                    .collection("waitlist").document(uid)
                waitRef.set(
                    mapOf(
                        "uid" to uid,
                        "name" to (profileName ?: auth.currentUser?.displayName ?: "Student"),
                        "email" to (profileEmail ?: auth.currentUser?.email ?: ""),
                        "waitAt" to System.currentTimeMillis(),
                        "referredBy" to referredBy
                    )
                ).await()
                // Inform caller that user was waitlisted via exception message
                throw java.lang.Exception("Event is full — you've been added to the waitlist.")
            }

            // Proceed to add attendee transactionally
            firestore.runTransaction { tx ->
                tx.set(attendeeRef, attendee)
                tx.set(
                    firestore.collection("users").document(uid).collection("myRsvps").document(eventId),
                    mapOf(
                        "eventId" to eventId,
                        "rsvpAt" to System.currentTimeMillis()
                    )
                )
                val current = (eventSnapshot.getLong("rsvpCount") ?: 0L)
                tx.update(eventRef, "rsvpCount", current + 1L)
            }.await()
        } catch (e: Exception) {
            val msg = when (e) {
                is com.google.firebase.firestore.FirebaseFirestoreException -> when (e.code) {
                    com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> "You don't have permission to RSVP to this event."
                    else -> e.message
                }
                else -> e.message
            }
            throw java.lang.Exception(msg)
        }
    }

    override suspend fun markAttendeeCheckedIn(eventId: String, attendeeUid: String): Result<Unit> = runCatching {
        val currentUid = auth.currentUser?.uid ?: error("Not authenticated")
        val eventRef = firestore.collection("events").document(eventId)
        val eventSnapshot = eventRef.get().await()
        val hostUid = eventSnapshot.getString("hostUid").orEmpty()
        if (hostUid != currentUid) error("Only the host can check in attendees")

        val attendeeRef = firestore.collection("rsvps").document(eventId)
            .collection("attendees").document(attendeeUid)
        attendeeRef.update("checkInStatus", true).await()
    }

    override suspend fun checkInWithTicket(eventId: String, ticketId: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        // Only hosts can check in via ticket
        val eventRef = firestore.collection("events").document(eventId)
        val eventSnapshot = eventRef.get().await()
        val hostUid = eventSnapshot.getString("hostUid").orEmpty()
        if (hostUid != uid) error("Only the host can check in attendees")

        val query = firestore.collection("rsvps").document(eventId).collection("attendees")
            .whereEqualTo("ticketId", ticketId).limit(1).get().await()
        val doc = query.documents.firstOrNull() ?: error("Ticket not found")
        val attendeeRef = doc.reference
        attendeeRef.update("checkInStatus", true).await()
    }

    override suspend fun saveDraft(draft: EventDraft): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        // Validation: basic draft sanity checks
        val capacityInt = draft.capacity.toIntOrNull() ?: 0
        if (draft.title.isBlank()) error("Title is required")
        if (capacityInt < 1) error("Capacity must be at least 1")

        try {
            val doc = firestore.collection("events").document()
            doc.set(draft.toMap(uid, "draft")).await()
            doc.id
        } catch (e: Exception) {
            val msg = if (e is com.google.firebase.firestore.FirebaseFirestoreException
                && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                "You don't have permission to save this draft."
            } else {
                e.message
            }
            throw java.lang.Exception(msg)
        }
    }

    override suspend fun publishEvent(draftId: String, draft: EventDraft): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        // Validation before publishing
        val capacityInt = draft.capacity.toIntOrNull() ?: 0
        if (draft.title.isBlank()) error("Title is required")
        if (capacityInt < 1) error("Capacity must be at least 1")
        if (draft.dateTime <= System.currentTimeMillis()) error("Event date must be in the future")

        try {
            val doc = if (draftId.isBlank()) firestore.collection("events").document() else firestore.collection("events").document(draftId)
            doc.set(draft.toMap(uid, "live")).await()
            doc.id
        } catch (e: Exception) {
            val msg = if (e is com.google.firebase.firestore.FirebaseFirestoreException
                && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                "You don't have permission to publish this event."
            } else {
                e.message
            }
            throw java.lang.Exception(msg)
        }
    }

    override suspend fun generateReferralLink(eventId: String, uid: String): Result<String> = runCatching {
        "https://paperlike.app/events/$eventId?ref=$uid"
    }

    override fun observeMyUpcoming(uid: String): Flow<List<Event>> = callbackFlow {
        val now = System.currentTimeMillis()
        val listener = firestore.collection("users").document(uid)
            .collection("myRsvps")
            .addSnapshotListener { snap, _ ->
                val eventIds = snap?.documents.orEmpty().map { it.id }
                if (eventIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    // Firestore whereIn limit is 30 items — chunk to stay safe
                    val allEvents = mutableListOf<Event>()
                    val chunks = eventIds.chunked(30)
                    var completed = 0
                    chunks.forEach { chunk ->
                        firestore.collection("events")
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                            .get()
                            .addOnSuccessListener { eventsSnap ->
                                allEvents.addAll(eventsSnap.documents.map { it.toEvent() })
                                completed++
                                if (completed == chunks.size) {
                                    val filtered = allEvents
                                        .filter { it.status == EventStatus.LIVE && it.dateTime > now }
                                        .sortedBy { it.dateTime }
                                    trySend(filtered)
                                }
                            }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeMyPast(uid: String): Flow<List<Event>> = callbackFlow {
        val now = System.currentTimeMillis()
        val listener = firestore.collection("users").document(uid)
            .collection("myRsvps")
            .addSnapshotListener { snap, _ ->
                val eventIds = snap?.documents.orEmpty().map { it.id }
                if (eventIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    val allEvents = mutableListOf<Event>()
                    val chunks = eventIds.chunked(30)
                    var completed = 0
                    chunks.forEach { chunk ->
                        firestore.collection("events")
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                            .get()
                            .addOnSuccessListener { eventsSnap ->
                                allEvents.addAll(eventsSnap.documents.map { it.toEvent() })
                                completed++
                                if (completed == chunks.size) {
                                    val filtered = allEvents
                                        .filter { it.status == EventStatus.PAST || it.dateTime <= now }
                                        .sortedByDescending { it.dateTime }
                                    trySend(filtered)
                                }
                            }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeMyHosted(uid: String): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("hostUid", uid)
            .addSnapshotListener { snap, _ ->
                val events = snap?.documents.orEmpty()
                    .map { it.toEvent() }
                    .sortedByDescending { it.dateTime }
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    override fun observeEvent(eventId: String): Flow<Event?> = callbackFlow {
        val listener = firestore.collection("events").document(eventId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toEvent())
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeEventAttendees(eventId: String): Flow<List<EventAttendee>> = callbackFlow {
        val listener = firestore.collection("rsvps").document(eventId)
            .collection("attendees")
            .orderBy("rsvpAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val attendees = snap?.documents.orEmpty().map { doc ->
                    EventAttendee(
                        uid = doc.id,
                        name = doc.getString("name").orEmpty().ifBlank { "Student" },
                        email = doc.getString("email").orEmpty(),
                        rsvpAt = doc.getLong("rsvpAt") ?: 0L,
                        checkInStatus = doc.getBoolean("checkInStatus") ?: false,
                        ticketId = doc.getString("ticketId").orEmpty()
                    )
                }
                trySend(attendees)
            }
        awaitClose { listener.remove() }
    }

    override fun observeWaitlistPosition(eventId: String, uid: String): Flow<Int?> = callbackFlow {
        val listener = firestore.collection("rsvps").document(eventId)
            .collection("waitlist")
            .orderBy("waitAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val docs = snap?.documents.orEmpty()
                val index = docs.indexOfFirst { it.id == uid }
                if (index == -1) trySend(null) else trySend(index + 1)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun removeFromWaitlist(eventId: String, uid: String): Result<Unit> = runCatching {
        firestore.collection("rsvps").document(eventId)
            .collection("waitlist").document(uid).delete().await()
    }

    override fun observeRecapWall(eventId: String): Flow<List<RecapPost>> = callbackFlow {
        val listener = firestore.collection("eventWall").document(eventId).collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val posts = snap?.documents.orEmpty().map { doc ->
                    RecapPost(
                        postId = doc.id,
                        uid = doc.getString("uid").orEmpty(),
                        imageUrl = doc.getString("imageUrl").orEmpty(),
                        caption = doc.getString("caption").orEmpty(),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        isPinned = doc.getBoolean("isPinned") ?: false
                    )
                }
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun postRecap(eventId: String, caption: String, imageUrl: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        firestore.collection("eventWall").document(eventId).collection("posts").document().set(
            mapOf(
                "uid" to uid,
                "imageUrl" to imageUrl,
                "caption" to caption,
                "createdAt" to System.currentTimeMillis(),
                "isPinned" to false
            )
        ).await()
    }

    override suspend fun removeAttendee(eventId: String, attendeeUid: String): Result<Unit> = runCatching {
        val currentUid = auth.currentUser?.uid ?: error("Not authenticated")
        val eventRef = firestore.collection("events").document(eventId)
        val attendeeRef = firestore.collection("rsvps").document(eventId)
            .collection("attendees").document(attendeeUid)
        val userRsvpRef = firestore.collection("users").document(attendeeUid)
            .collection("myRsvps").document(eventId)

        val eventSnapshot = eventRef.get().await()
        val hostUid = eventSnapshot.getString("hostUid").orEmpty()
        if (hostUid != currentUid) error("Only the host can remove attendees")

        // Remove attendee and their myRsvps entry
        attendeeRef.delete().await()
        userRsvpRef.delete().await()

        // Check for waitlisted users to promote
        val waitQuery = firestore.collection("rsvps").document(eventId)
            .collection("waitlist").orderBy("waitAt", Query.Direction.ASCENDING).limit(1)
        val waitSnap = waitQuery.get().await()
        if (waitSnap.isEmpty) {
            // No waitlist — decrement rsvpCount
            firestore.runTransaction { tx ->
                val current = tx.get(eventRef).getLong("rsvpCount") ?: 0L
                tx.update(eventRef, "rsvpCount", (current - 1L).coerceAtLeast(0L))
            }.await()
        } else {
            // Promote the first waitlisted user
            val doc = waitSnap.documents.first()
            val promotedUid = doc.id
            val promotedName = doc.getString("name").orEmpty()
            val promotedEmail = doc.getString("email").orEmpty()

            // Create attendee for promoted user and add their myRsvps entry, then remove waitlist entry
            val promotedAttendeeRef = firestore.collection("rsvps").document(eventId)
                .collection("attendees").document(promotedUid)
            val promotedUserRsvpRef = firestore.collection("users").document(promotedUid)
                .collection("myRsvps").document(eventId)

            firestore.runTransaction { tx ->
                tx.set(promotedAttendeeRef, mapOf(
                    "uid" to promotedUid,
                    "name" to promotedName,
                    "email" to promotedEmail,
                    "rsvpAt" to System.currentTimeMillis(),
                    "checkInStatus" to false,
                    "ticketId" to java.util.UUID.randomUUID().toString()
                ))
                tx.set(promotedUserRsvpRef, mapOf(
                    "eventId" to eventId,
                    "rsvpAt" to System.currentTimeMillis()
                ))

                val current = tx.get(eventRef).getLong("rsvpCount") ?: 0L
                tx.update(eventRef, mapOf(
                    "rsvpCount" to current + 1L,
                    "lastPromotion" to mapOf(
                        "uid" to promotedUid,
                        "name" to promotedName,
                        "at" to System.currentTimeMillis()
                    )
                ))
            }.await()

            // Delete the waitlist doc after promotion
            firestore.collection("rsvps").document(eventId)
                .collection("waitlist").document(promotedUid).delete().await()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEvent(): Event {
        return Event(
            eventId = id,
            title = getString("title").orEmpty(),
            description = getString("description").orEmpty(),
            hostUid = getString("hostUid").orEmpty(),
            hostName = getString("hostName").orEmpty(),
            coverImageUrl = getString("coverImageUrl").orEmpty(),
            category = getString("category").orEmpty(),
            vibes = (get("vibes") as? List<String>).orEmpty().mapNotNull { value ->
                runCatching { VibeTag.valueOf(value.uppercase()) }.getOrNull()
            },
            dateTime = getLong("dateTime") ?: System.currentTimeMillis(),
            venue = getString("venue").orEmpty(),
            capacity = (getLong("capacity") ?: 0L).toInt(),
            rsvpCount = (getLong("rsvpCount") ?: 0L).toInt(),
            tags = get("tags") as? List<String> ?: emptyList(),
            status = when (getString("status")) {
                "live" -> EventStatus.LIVE
                "past" -> EventStatus.PAST
                else -> EventStatus.DRAFT
            },
            isPaid = getBoolean("isPaid") ?: false,
            price = getString("price").orEmpty(),
            socialProof = RSVPActivity(uid = "", name = "", timestamp = 0L),
            lastPromotion = runCatching {
                val map = get("lastPromotion") as? Map<*, *>
                if (map == null) null else Promotion(
                    uid = map["uid"].toString(),
                    name = map["name"].toString(),
                    at = (map["at"] as? Long) ?: 0L
                )
            }.getOrNull(),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    private fun EventDraft.toMap(hostUid: String, status: String): Map<String, Any> {
        return mapOf(
            "title" to title,
            "description" to description,
            "hostUid" to hostUid,
            "hostName" to (auth.currentUser?.displayName ?: "Event Host"),
            "coverImageUrl" to coverImageUrl,
            "category" to category,
            "vibes" to vibes.map { it.name.lowercase() },
            "dateTime" to dateTime,
            "venue" to venue,
            "capacity" to (capacity.toIntOrNull() ?: 0),
            "rsvpCount" to 0,
            "tags" to tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
            "status" to status,
            "isPaid" to isPaid,
            "price" to price,
            "createdAt" to System.currentTimeMillis()
        )
    }
}
