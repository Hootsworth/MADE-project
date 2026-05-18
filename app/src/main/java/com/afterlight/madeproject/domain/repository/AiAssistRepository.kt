package com.afterlight.madeproject.domain.repository

import com.afterlight.madeproject.domain.model.AiDraftEnhancement
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventDraft

interface AiAssistRepository {
    suspend fun polishEventDraft(draft: EventDraft): Result<AiDraftEnhancement>
    suspend fun polishRecapCaption(caption: String): Result<String>
    suspend fun summarizeFeed(events: List<Event>): Result<String>
}
