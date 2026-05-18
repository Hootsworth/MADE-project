package com.afterlight.madeproject.domain.model

import java.time.Instant

enum class UserRole { STUDENT, HOST }

enum class EventStatus { DRAFT, LIVE, PAST }

enum class VibeTag {
    CHILL,
    HYPE,
    INTELLECTUAL,
    CHAOTIC,
    COZY,
    PROFESSIONAL
}

enum class AiProvider {
    OPENAI,
    GEMINI
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val year: String = "",
    val department: String = "",
    val interests: List<String> = emptyList(),
    val role: UserRole = UserRole.STUDENT,
    val referralCode: String = "",
    val badgesEarned: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class RSVPActivity(
    val uid: String,
    val name: String,
    val timestamp: Long
)

data class EventAttendee(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val rsvpAt: Long = 0L,
    val checkInStatus: Boolean = false
)

data class Event(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val hostUid: String = "",
    val hostName: String = "",
    val coverImageUrl: String = "",
    val category: String = "",
    val vibes: List<VibeTag> = emptyList(),
    val dateTime: Long = Instant.now().toEpochMilli(),
    val venue: String = "",
    val capacity: Int = 0,
    val rsvpCount: Int = 0,
    val tags: List<String> = emptyList(),
    val status: EventStatus = EventStatus.DRAFT,
    val isPaid: Boolean = false,
    val price: String = "",
    val socialProof: RSVPActivity? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val spotsLeft: Int get() = (capacity - rsvpCount).coerceAtLeast(0)
}

data class DepartmentScore(
    val departmentId: String,
    val name: String,
    val monthlyScore: Int,
    val eventCount: Int,
    val attendeeCount: Int
)

data class RecapPost(
    val postId: String,
    val uid: String,
    val imageUrl: String,
    val caption: String,
    val createdAt: Long,
    val isPinned: Boolean
)

data class EventDraft(
    val coverImageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val vibes: List<VibeTag> = emptyList(),
    val dateTime: Long = Instant.now().toEpochMilli(),
    val venue: String = "",
    val capacity: String = "100",
    val tags: String = "",
    val isRsvp: Boolean = true,
    val isPaid: Boolean = false,
    val price: String = ""
)

data class AiSettings(
    val provider: AiProvider = AiProvider.OPENAI,
    val openAiApiKey: String = "",
    val geminiApiKey: String = "",
    val model: String = "gpt-4.1-mini"
)

data class AiDraftEnhancement(
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String>,
    val vibes: List<String>,
    val reasoning: String
)
