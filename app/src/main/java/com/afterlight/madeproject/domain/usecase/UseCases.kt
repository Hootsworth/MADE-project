package com.afterlight.madeproject.domain.usecase

import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.repository.AuthRepository
import com.afterlight.madeproject.domain.repository.EventRepository
import com.afterlight.madeproject.domain.repository.UserRepository
import javax.inject.Inject

class ValidateCollegeEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Boolean> =
        authRepository.validateCollegeDomain(email)
}

class ObserveHomeFeedUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke() = eventRepository.observeFeaturedAndFeed(interests = emptyList())
}

class SearchEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(
        query: String,
        category: String?,
        department: String?,
        dateStart: Long?,
        dateEnd: Long?,
        freeOnly: Boolean,
        minCapacity: Int?,
        vibes: List<String>
    ) = eventRepository.searchEvents(
        query = query,
        category = category,
        department = department,
        dateStart = dateStart,
        dateEnd = dateEnd,
        freeOnly = freeOnly,
        minCapacity = minCapacity,
        vibes = vibes
    )
}

class SaveHostDraftUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(draft: EventDraft) = eventRepository.saveDraft(draft)
}

class PublishEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(draftId: String, draft: EventDraft) = eventRepository.publishEvent(draftId, draft)
}

class RSVPEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, referredBy: String?) = eventRepository.rsvp(eventId, referredBy)
}
