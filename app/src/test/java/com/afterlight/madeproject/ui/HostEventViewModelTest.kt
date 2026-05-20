package com.afterlight.madeproject.ui

import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.domain.model.DepartmentScore
import com.afterlight.madeproject.domain.model.RecapPost
import com.afterlight.madeproject.domain.model.RSVPActivity
import com.afterlight.madeproject.domain.model.VibeTag
import com.afterlight.madeproject.domain.repository.AiAssistRepository
import com.afterlight.madeproject.domain.repository.EventRepository
import com.afterlight.madeproject.domain.repository.UserRepository
import com.afterlight.madeproject.domain.usecase.SaveHostDraftUseCase
import com.afterlight.madeproject.data.repository.ExternalApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HostEventViewModelTest {

    private val fakeEventRepository = object : EventRepository {
        override fun observeFeaturedAndFeed(interests: List<String>) = kotlinx.coroutines.flow.flowOf(emptyList<Event>())
        override fun observeHappeningThisWeek() = kotlinx.coroutines.flow.flowOf(emptyList<Event>())
        override fun observeSocialProofTicker(eventId: String) = kotlinx.coroutines.flow.flowOf("")
        override fun observeLeaderboard() = kotlinx.coroutines.flow.flowOf(emptyList<DepartmentScore>())
        override fun searchEvents(query: String, category: String?, department: String?, dateStart: Long?, dateEnd: Long?, freeOnly: Boolean, minCapacity: Int?, vibes: List<String>) = kotlinx.coroutines.flow.flowOf(emptyList<Event>())
        override suspend fun rsvp(eventId: String, referredBy: String?) = Result.success(Unit)
        override suspend fun saveDraft(draft: EventDraft) = Result.success("draft-id")
        override suspend fun publishEvent(draftId: String, draft: EventDraft) = Result.success("event-id")
        override suspend fun generateReferralLink(eventId: String, uid: String) = Result.success("")
        override fun observeMyUpcoming(uid: String) = kotlinx.coroutines.flow.flowOf(emptyList<Event>())
        override fun observeMyPast(uid: String) = kotlinx.coroutines.flow.flowOf(emptyList<Event>())
        override fun observeMyHosted(uid: String) = kotlinx.coroutines.flow.flowOf(emptyList<Event>())
        override fun observeEvent(eventId: String) = kotlinx.coroutines.flow.flowOf(null as Event?)
        override fun observeEventAttendees(eventId: String) = kotlinx.coroutines.flow.flowOf(emptyList<EventAttendee>())
        override fun observeRecapWall(eventId: String) = kotlinx.coroutines.flow.flowOf(emptyList<RecapPost>())
        override fun observeWaitlistPosition(eventId: String, uid: String) = kotlinx.coroutines.flow.flowOf(null as Int?)
        override suspend fun removeAttendee(eventId: String, attendeeUid: String) = Result.success(Unit)
        override suspend fun postRecap(eventId: String, caption: String, imageUrl: String) = Result.success(Unit)
        override suspend fun removeFromWaitlist(eventId: String, uid: String) = Result.success(Unit)
        override suspend fun markAttendeeCheckedIn(eventId: String, attendeeUid: String) = Result.success(Unit)
        override suspend fun checkInWithTicket(eventId: String, ticketId: String) = Result.success(Unit)
    }

    private val fakeAi = object : AiAssistRepository {
        override suspend fun polishEventDraft(draft: EventDraft) = Result.failure<Nothing>(NotImplementedError())
        override suspend fun polishRecapCaption(caption: String) = Result.failure<Nothing>(NotImplementedError())
        override suspend fun summarizeFeed(events: List<Event>) = Result.failure<Nothing>(NotImplementedError())
    }
    private val fakeUserRepo = object : UserRepository {
        override fun observeCurrentUser() = kotlinx.coroutines.flow.flowOf(null)
        override suspend fun saveProfile(profile: com.afterlight.madeproject.domain.model.UserProfile) = Result.success(Unit)
        override suspend fun getCurrentUser() = null
    }
    private val fakeExternal = ExternalApiService()

    @Test
    fun updateDraft_updatesState() = runBlocking {
        val saveUseCase = SaveHostDraftUseCase(fakeEventRepository)
        val vm = HostEventViewModel(saveUseCase, fakeEventRepository, fakeAi, fakeUserRepo, fakeExternal)

        val newDraft = EventDraft(title = "Test Event")
        vm.updateDraft(newDraft)

        assertEquals("Test Event", vm.draft.value.title)
    }
}
