package com.afterlight.madeproject.domain.repository

import com.afterlight.madeproject.domain.model.DepartmentScore
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.RecapPost
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeFeaturedAndFeed(interests: List<String>): Flow<List<Event>>
    fun observeHappeningThisWeek(): Flow<List<Event>>
    fun observeSocialProofTicker(eventId: String): Flow<String>
    fun observeLeaderboard(): Flow<List<DepartmentScore>>
    fun searchEvents(
        query: String,
        category: String?,
        department: String?,
        dateStart: Long?,
        dateEnd: Long?,
        freeOnly: Boolean,
        minCapacity: Int?,
        vibes: List<String>
    ): Flow<List<Event>>

    suspend fun rsvp(eventId: String, referredBy: String?): Result<Unit>
    suspend fun saveDraft(draft: EventDraft): Result<String>
    suspend fun publishEvent(draftId: String, draft: EventDraft): Result<String>
    suspend fun generateReferralLink(eventId: String, uid: String): Result<String>

    fun observeMyUpcoming(uid: String): Flow<List<Event>>
    fun observeMyPast(uid: String): Flow<List<Event>>
    fun observeMyHosted(uid: String): Flow<List<Event>>

    fun observeEvent(eventId: String): Flow<Event?>
    fun observeEventAttendees(eventId: String): Flow<List<EventAttendee>>
    fun observeRecapWall(eventId: String): Flow<List<RecapPost>>
    suspend fun removeAttendee(eventId: String, attendeeUid: String): Result<Unit>
    suspend fun postRecap(eventId: String, caption: String, imageUrl: String): Result<Unit>
}
