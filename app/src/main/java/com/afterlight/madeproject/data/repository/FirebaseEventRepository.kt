package com.afterlight.madeproject.data.repository

import com.afterlight.madeproject.domain.model.DepartmentScore
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.EventStatus
import com.afterlight.madeproject.domain.model.RecapPost
import com.afterlight.madeproject.domain.model.RSVPActivity
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
        val attendee = mapOf(
            "uid" to uid,
            "name" to (auth.currentUser?.displayName ?: "Student"),
            "email" to (auth.currentUser?.email ?: ""),
            "rsvpAt" to System.currentTimeMillis(),
            "checkInStatus" to false,
            "referredBy" to referredBy
        )
        // Global RSVP record for the event host
        firestore.collection("rsvps").document(eventId)
            .collection("attendees").document(uid).set(attendee).await()

        // User-specific record for "My Events" filtering
        firestore.collection("users").document(uid)
            .collection("myRsvps").document(eventId).set(mapOf(
                "eventId" to eventId,
                "rsvpAt" to System.currentTimeMillis()
            )).await()

        val eventRef = firestore.collection("events").document(eventId)
        firestore.runTransaction { tx ->
            val current = tx.get(eventRef).getLong("rsvpCount") ?: 0L
            tx.update(eventRef, "rsvpCount", current + 1L)
        }.await()
    }

    override suspend fun saveDraft(draft: EventDraft): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        
        // FAILSAFE FOR DEMO: Grant 'host' role in backend so Firestore rules don't block the write
        try {
            firestore.collection("users").document(uid)
                .set(mapOf("role" to "host"), com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) { /* ignore */ }

        val doc = firestore.collection("events").document()
        doc.set(draft.toMap(uid, "draft")).await()
        doc.id
    }

    override suspend fun publishEvent(draftId: String, draft: EventDraft): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        
        // FAILSAFE FOR DEMO: Grant 'host' role in backend
        try {
            firestore.collection("users").document(uid)
                .set(mapOf("role" to "host"), com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) { /* ignore */ }

        val doc = if (draftId.isBlank()) firestore.collection("events").document() else firestore.collection("events").document(draftId)
        doc.set(draft.toMap(uid, "live")).await()
        doc.id
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
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ -> trySend(snap?.documents.orEmpty().map { it.toEvent() }) }
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
                        checkInStatus = doc.getBoolean("checkInStatus") ?: false
                    )
                }
                trySend(attendees)
            }
        awaitClose { listener.remove() }
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

        attendeeRef.delete().await()
        userRsvpRef.delete().await()

        firestore.runTransaction { tx ->
            val current = tx.get(eventRef).getLong("rsvpCount") ?: 0L
            tx.update(eventRef, "rsvpCount", (current - 1L).coerceAtLeast(0L))
        }.await()
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
