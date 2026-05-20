package com.afterlight.madeproject.ui

import androidx.camera.core.ImageProxy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterlight.madeproject.data.repository.ExternalApiService
import com.afterlight.madeproject.domain.model.AiDraftEnhancement
import com.afterlight.madeproject.domain.model.AiProvider
import com.afterlight.madeproject.domain.model.AiSettings
import com.afterlight.madeproject.domain.model.DepartmentScore
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventStatus
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.RecapPost
import com.afterlight.madeproject.domain.model.UserProfile
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.domain.model.ThemeMode
import com.afterlight.madeproject.domain.model.VibeTag
import com.afterlight.madeproject.domain.repository.AiAssistRepository
import com.afterlight.madeproject.domain.repository.AuthRepository
import com.afterlight.madeproject.domain.repository.EventRepository
import com.afterlight.madeproject.domain.repository.SettingsRepository
import com.afterlight.madeproject.domain.repository.UserRepository
import com.afterlight.madeproject.domain.usecase.RSVPEventUseCase
import com.afterlight.madeproject.domain.usecase.SaveHostDraftUseCase
import com.afterlight.madeproject.domain.usecase.ValidateCollegeEmailUseCase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ────────────────────────────────────────────────────────────────────────────────
// Launch
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class LaunchViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    authRepository: AuthRepository,
    userRepository: UserRepository
) : ViewModel() {
    val destination: StateFlow<String> = combine(
        settingsRepository.onboardingDone(),
        authRepository.authStateChanges(),
        userRepository.observeCurrentUser()
    ) { onboardingDone, uid, profile ->
        when {
            !onboardingDone -> Routes.Onboarding
            uid == null -> Routes.Auth
            profile == null -> Routes.ProfileSetup
            else -> Routes.Home
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Routes.Onboarding)
}

// ────────────────────────────────────────────────────────────────────────────────
// Theme
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode = settingsRepository.themeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Auth
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateCollegeEmailUseCase: ValidateCollegeEmailUseCase,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) = _uiState.update { it.copy(mode = mode, error = null) }
    fun setNextAction(nextAction: AuthNextAction?) = _uiState.update { it.copy(nextAction = nextAction) }
    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value) }

    fun login() = submitAuth(signUp = false)
    fun signUp() = submitAuth(signUp = true)

    fun continueInTestMode() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val result = authRepository.signInAnonymously()
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = result.exceptionOrNull()?.message ?: "Test mode sign-in failed"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(loading = false, nextAction = AuthNextAction.PROFILE_SETUP) }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val authResult = authRepository.signInWithGoogleIdToken(idToken)
            if (authResult.isFailure) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = authResult.exceptionOrNull()?.message ?: "Google sign in failed"
                    )
                }
                return@launch
            }

            val email = authRepository.currentEmail().orEmpty()
            val profile = userRepository.getCurrentUser()
            _uiState.update {
                it.copy(
                    loading = false,
                    emailVerified = true,
                    email = email,
                    nextAction = if (profile == null) AuthNextAction.PROFILE_SETUP else AuthNextAction.HOME
                )
            }
        }
    }

    private fun submitAuth(signUp: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val state = _uiState.value

            val authResult = if (signUp) {
                if (state.name.isBlank()) {
                    _uiState.update { it.copy(loading = false, error = "Name is required for sign up") }
                    return@launch
                }
                if (state.password != state.confirmPassword) {
                    _uiState.update { it.copy(loading = false, error = "Passwords do not match") }
                    return@launch
                }
                authRepository.signUp(state.name.trim(), state.email.trim(), state.password)
            } else {
                authRepository.login(state.email.trim(), state.password)
            }

            if (authResult.isFailure) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = authResult.exceptionOrNull()?.message ?: "Authentication failed"
                    )
                }
                return@launch
            }

            val profile = userRepository.getCurrentUser()
            _uiState.update {
                it.copy(
                    loading = false,
                    nextAction = if (profile == null) AuthNextAction.PROFILE_SETUP else AuthNextAction.HOME
                )
            }
        }
    }
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val emailVerified: Boolean = false,
    val nextAction: AuthNextAction? = null
)

enum class AuthMode { LOGIN, SIGN_UP }
enum class AuthNextAction { HOME, PROFILE_SETUP }

// ────────────────────────────────────────────────────────────────────────────────
// Profile Setup
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    val externalApiService: ExternalApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = authRepository.currentDisplayName().orEmpty()
            val email = authRepository.currentEmail().orEmpty()
            _state.update { it.copy(name = name, email = email) }
        }
    }

    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setYear(year: String) = _state.update { it.copy(year = year) }
    fun setSchool(school: String) = _state.update { it.copy(school = school) }

    fun toggleInterest(tag: String) {
        val next = _state.value.interests.toMutableSet()
        if (next.contains(tag)) next.remove(tag) else next.add(tag)
        _state.update { it.copy(interests = next.toList()) }
    }

    fun setRole(role: UserRole) {
        _state.update { it.copy(role = role) }
    }

    fun saveProfile(onSaved: () -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.saving) return@launch

            _state.update { it.copy(saving = true, error = null) }

            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(saving = false, error = "You are not logged in. Please sign in again.") }
                return@launch
            }

            val finalName = if (currentState.name.isBlank() && authRepository.isAnonymous()) "Test User" else currentState.name
            val finalSchool = if (currentState.school.isBlank() && authRepository.isAnonymous()) "Test School" else currentState.school
            val finalYear = if (currentState.year.isBlank() && authRepository.isAnonymous()) "2024" else currentState.year

            if (finalName.isBlank()) {
                _state.update { it.copy(saving = false, error = "Name is required.") }
                return@launch
            }
            if (finalSchool.isBlank()) {
                _state.update { it.copy(saving = false, error = "School is required.") }
                return@launch
            }
            if (finalYear.isBlank()) {
                _state.update { it.copy(saving = false, error = "Academic year is required.") }
                return@launch
            }

            val email = currentState.email.ifBlank { "anonymous@test.edu.in" }
            val profile = UserProfile(
                uid = uid,
                email = email,
                name = finalName,
                year = finalYear,
                department = finalSchool,
                interests = currentState.interests,
                role = currentState.role,
                referralCode = uid.take(6).uppercase()
            )
            val result = userRepository.saveProfile(profile)
            if (result.isSuccess) {
                settingsRepository.setOnboardingDone(true)
                _state.update { it.copy(saving = false, error = null) }
                onSaved()
            } else {
                _state.update {
                    it.copy(
                        saving = false,
                        error = result.exceptionOrNull()?.message ?: "Could not save profile. Try again."
                    )
                }
            }
        }
    }
}

data class ProfileSetupState(
    val email: String = "",
    val name: String = "",
    val year: String = "",
    val school: String = "",
    val interests: List<String> = emptyList(),
    val role: UserRole = UserRole.STUDENT,
    val saving: Boolean = false,
    val error: String? = null
)

// ────────────────────────────────────────────────────────────────────────────────
// Home
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val aiAssistRepository: AiAssistRepository,
    private val userRepository: com.afterlight.madeproject.domain.repository.UserRepository,
    val externalApiService: ExternalApiService
) : ViewModel() {

    val profile = userRepository.observeCurrentUser()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val feed: StateFlow<List<Event>> = kotlinx.coroutines.flow.combine(
        eventRepository.observeFeaturedAndFeed(emptyList()),
        userRepository.observeCurrentUser()
    ) { events, user ->
        val activeEvents = events.filter { event ->
            event.status == EventStatus.LIVE && event.dateTime > System.currentTimeMillis()
        }

        if (user == null || user.interests.isEmpty()) {
            activeEvents
        } else {
            activeEvents.sortedByDescending { event ->
                val matchingVibes = event.vibes.count { vibe ->
                    user.interests.any { it.equals(vibe.name, ignoreCase = true) }
                }
                val matchingTags = event.tags.count { tag ->
                    user.interests.any { it.equals(tag, ignoreCase = true) }
                }
                (matchingVibes * 2) + matchingTags
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val week = eventRepository.observeHappeningThisWeek()
        .map { events ->
            val now = System.currentTimeMillis()
            events.filter { event ->
                event.status == EventStatus.LIVE && event.dateTime > now
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiBrief = MutableStateFlow("Loading campus brief...")
    val aiBrief: StateFlow<String> = _aiBrief.asStateFlow()

    private val _aiBusy = MutableStateFlow(false)
    val aiBusy: StateFlow<Boolean> = _aiBusy.asStateFlow()

    private val _dailyQuote = MutableStateFlow<Pair<String, String>?>(null)
    val dailyQuote: StateFlow<Pair<String, String>?> = _dailyQuote.asStateFlow()

    init {
        viewModelScope.launch {
            val result = externalApiService.fetchRandomQuote()
            result.onSuccess { _dailyQuote.value = it }
        }

        viewModelScope.launch {
            feed.collect { events ->
                if (events.isNotEmpty() && _aiBrief.value.startsWith("Loading")) {
                    refreshAiBrief()
                }
            }
        }
    }

    fun refreshAiBrief() {
        viewModelScope.launch {
            val events = feed.value.take(8)
            if (events.isEmpty()) {
                val quote = _dailyQuote.value
                _aiBrief.value = if (quote != null) {
                    "\"${quote.first}\" — ${quote.second}"
                } else {
                    "No live events yet."
                }
                return@launch
            }
            _aiBusy.value = true
            val result = aiAssistRepository.summarizeFeed(events)
            _aiBrief.value = result.getOrElse { "Top pick: ${events.first().title}" }
            _aiBusy.value = false
        }
    }

    fun greeting(): String {
        val hour = java.time.LocalTime.now().hour
        return when {
            hour < 12 -> "GOOD MORNING"
            hour < 17 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Discover
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _vibes = MutableStateFlow<List<String>>(emptyList())
    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    val leaderboard = eventRepository.observeLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val searchResults: StateFlow<List<Event>> = combine(_query, _category, _vibes) { q, c, v ->
        Triple(q, c, v)
    }.flatMapLatest { (query, category, vibes) ->
        eventRepository.searchEvents(
            query = query,
            category = category,
            department = null,
            dateStart = null,
            dateEnd = null,
            freeOnly = false,
            minCapacity = null,
            vibes = vibes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(value: String) { _query.value = value }

    fun toggleVibe(vibe: String) {
        val set = _vibes.value.toMutableSet()
        if (!set.add(vibe)) set.remove(vibe)
        _vibes.value = set.toList()
    }

    fun setCategory(category: String?) {
        _category.value = category
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Event Detail
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val rsvpEventUseCase: RSVPEventUseCase,
    private val authRepository: com.afterlight.madeproject.domain.repository.AuthRepository,
    val externalApiService: ExternalApiService
) : ViewModel() {

    val eventId: String = savedStateHandle["eventId"] ?: ""

    val ticker = eventRepository.observeSocialProofTicker(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading activity...")

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    private val _rsvpDone = MutableStateFlow(false)
    val rsvpDone: StateFlow<Boolean> = _rsvpDone.asStateFlow()
    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()
    val waitlistPosition = kotlinx.coroutines.flow.flow {
        val uid = authRepository.currentUid()
        if (uid == null) {
            emit(null)
        } else {
            eventRepository.observeWaitlistPosition(eventId, uid).collect { emit(it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val myTicketId = kotlinx.coroutines.flow.flow {
        val uid = authRepository.currentUid()
        if (uid == null) {
            emit(null)
        } else {
            eventRepository.observeEventAttendees(eventId).collect { list ->
                emit(list.firstOrNull { it.uid == uid }?.ticketId)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hasRsvped = kotlinx.coroutines.flow.flow {
        val uid = authRepository.currentUid()
        if (uid == null) {
            emit(false)
        } else {
            eventRepository.observeEventAttendees(eventId).collect { list ->
                emit(list.any { it.uid == uid })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        if (eventId.isNotBlank()) {
            viewModelScope.launch {
                eventRepository.observeEvent(eventId).collect { fetchedEvent ->
                    _event.value = fetchedEvent
                }
            }
        }
    }

    fun setEvent(value: Event) { _event.value = value }

    fun rsvp(referredBy: String? = null, usn: String? = null) {
        viewModelScope.launch {
            val currentEvent = _event.value ?: return@launch
            val eventOver = currentEvent.status == EventStatus.PAST ||
                    currentEvent.dateTime <= System.currentTimeMillis()
            if (eventOver) return@launch
            val result = rsvpEventUseCase(eventId, referredBy)
            if (result.isSuccess) {
                _rsvpDone.value = true
                _status.value = "RSVP confirmed"
            } else {
                _status.value = result.exceptionOrNull()?.message ?: "Could not complete RSVP"
            }
        }
    }

    fun cancelWaitlist() {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch
            val result = eventRepository.removeFromWaitlist(eventId, uid)
            _status.value = if (result.isSuccess) "Removed from waitlist" else result.exceptionOrNull()?.message
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// QR Scan RSVP
// ────────────────────────────────────────────────────────────────────────────────
sealed interface QrScanLookupState {
    data object Idle : QrScanLookupState
    data object Loading : QrScanLookupState
    data class Ready(val event: Event) : QrScanLookupState
    data object NotFound : QrScanLookupState
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class QrScanRsvpViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val rsvpEventUseCase: RSVPEventUseCase
) : ViewModel() {

    private val _eventId = MutableStateFlow("")
    val eventId: StateFlow<String> = _eventId.asStateFlow()

    val eventLookupState: StateFlow<QrScanLookupState> = _eventId
        .flatMapLatest { id ->
            if (id.isBlank()) {
                flowOf(QrScanLookupState.Idle)
            } else {
                eventRepository.observeEvent(id)
                    .map { event ->
                        if (event == null) QrScanLookupState.NotFound else QrScanLookupState.Ready(event)
                    }
                    .onStart { emit(QrScanLookupState.Loading) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QrScanLookupState.Idle)

    val event: StateFlow<Event?> = eventLookupState
        .map { state -> (state as? QrScanLookupState.Ready)?.event }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _rsvpDone = MutableStateFlow(false)
    val rsvpDone: StateFlow<Boolean> = _rsvpDone.asStateFlow()

    private val _rsvpLoading = MutableStateFlow(false)
    val rsvpLoading: StateFlow<Boolean> = _rsvpLoading.asStateFlow()

    fun onCodeScanned(value: String): Boolean {
        val parsedEventId = parseEventId(value)
        if (parsedEventId.isBlank()) {
            _status.value = "That QR code is not a Paperlike event link."
            return false
        }
        _eventId.value = parsedEventId
        _rsvpDone.value = false
        _rsvpLoading.value = false
        _status.value = null
        return true
    }

    fun clearScan() {
        _eventId.value = ""
        _rsvpDone.value = false
        _rsvpLoading.value = false
        _status.value = null
    }

    fun rsvp() {
        val id = _eventId.value
        if (id.isBlank() || _rsvpLoading.value) return

        val currentEvent = (eventLookupState.value as? QrScanLookupState.Ready)?.event
        val eventOver = currentEvent?.let {
            it.status == EventStatus.PAST || it.dateTime <= System.currentTimeMillis()
        } == true
        if (eventOver) {
            _status.value = "This event has ended. RSVP is closed."
            return
        }

        viewModelScope.launch {
            _rsvpLoading.value = true
            val result = rsvpEventUseCase(id, null)
            _rsvpLoading.value = false
            if (result.isSuccess) {
                _rsvpDone.value = true
                _status.value = "RSVP confirmed. Your name and email were shared with the host."
            } else {
                _status.value = result.exceptionOrNull()?.message ?: "Could not RSVP to this event."
            }
        }
    }

    private fun parseEventId(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return ""
        val pathSegments = runCatching { android.net.Uri.parse(trimmed) }.getOrNull()
            ?.pathSegments
            .orEmpty()
        val eventIndex = pathSegments.indexOf("events")
        if (eventIndex >= 0 && pathSegments.size > eventIndex + 1) {
            return pathSegments[eventIndex + 1].trim()
        }

        return trimmed.takeIf {
            it.length >= 8 && it.all { character -> character.isLetterOrDigit() || character == '-' || character == '_' }
        }.orEmpty()
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Host Event
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class HostEventViewModel @Inject constructor(
    private val saveHostDraftUseCase: SaveHostDraftUseCase,
    private val eventRepository: EventRepository,
    private val aiAssistRepository: AiAssistRepository,
    private val userRepository: UserRepository,
    val externalApiService: ExternalApiService
) : ViewModel() {

    private val _draft = MutableStateFlow(EventDraft())
    val draft = _draft.asStateFlow()

    private val _draftId = MutableStateFlow("")

    val userProfile = userRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiReason = MutableStateFlow<String?>(null)
    val aiReason: StateFlow<String?> = _aiReason.asStateFlow()

    private val _publishError = MutableStateFlow<String?>(null)
    val publishError: StateFlow<String?> = _publishError.asStateFlow()

    fun updateDraft(newDraft: EventDraft) {
        _draft.value = newDraft
    }

    fun toggleVibe(vibeTag: VibeTag) {
        val list = _draft.value.vibes.toMutableSet()
        if (!list.add(vibeTag)) list.remove(vibeTag)
        _draft.update { it.copy(vibes = list.toList()) }
    }

    fun saveDraft() {
        viewModelScope.launch {
            val result = saveHostDraftUseCase(normalizedDraft(_draft.value))
            result.getOrNull()?.let { _draftId.value = it }
        }
    }

    fun publish(onPublished: (String) -> Unit) {
        viewModelScope.launch {
            if (_draftId.value.isBlank()) {
                val saveResult = saveHostDraftUseCase(normalizedDraft(_draft.value))
                if (saveResult.isFailure) {
                    _publishError.value = saveResult.exceptionOrNull()?.message ?: "Failed to save draft"
                    return@launch
                }
                _draftId.value = saveResult.getOrDefault("")
            }

            val result = eventRepository.publishEvent(_draftId.value, normalizedDraft(_draft.value))
            if (result.isSuccess) {
                result.getOrNull()?.let(onPublished)
            } else {
                _publishError.value = result.exceptionOrNull()?.message ?: "Failed to publish event"
            }
        }
    }

    private fun normalizedDraft(draft: EventDraft): EventDraft {
        val cleanedPrice = draft.price
            .trim()
            .replace(",", "")
            .replace("₹", "")
            .replace("$", "")
            .replace(" ", "")
        val priceValue = cleanedPrice.toDoubleOrNull() ?: 0.0
        return if (priceValue > 0.0) {
            draft.copy(price = cleanedPrice, isPaid = true)
        } else {
            draft.copy(price = "", isPaid = false)
        }
    }

    fun enhanceDraftWithAi() {
        viewModelScope.launch {
            _aiLoading.value = true
            val result = aiAssistRepository.polishEventDraft(_draft.value)
            result.onSuccess(::applyEnhancement)
            _aiReason.value = result.getOrNull()?.reasoning ?: result.exceptionOrNull()?.message
            _aiLoading.value = false
        }
    }

    fun generateCoverImage() {
        val keywords = _draft.value.category.ifBlank { "event,campus" }
        val url = externalApiService.unsplashCoverUrl(keywords)
        _draft.update { it.copy(coverImageUrl = url) }
    }

    private fun applyEnhancement(enhancement: AiDraftEnhancement) {
        val mappedVibes = enhancement.vibes.mapNotNull { value ->
            runCatching { VibeTag.valueOf(value.uppercase()) }.getOrNull()
        }
        _draft.update { current ->
            current.copy(
                title = enhancement.title.ifBlank { current.title },
                description = enhancement.description.ifBlank { current.description },
                category = enhancement.category.ifBlank { current.category },
                tags = enhancement.tags.joinToString(", ").ifBlank { current.tags },
                vibes = mappedVibes.ifEmpty { current.vibes }
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// My Events
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class MyEventsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    val externalApiService: ExternalApiService
) : ViewModel() {

    val profile = userRepository.observeCurrentUser()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents.asStateFlow()

    private val _pastEvents = MutableStateFlow<List<Event>>(emptyList())
    val pastEvents: StateFlow<List<Event>> = _pastEvents.asStateFlow()

    private val _hostedEvents = MutableStateFlow<List<Event>>(emptyList())
    val hostedEvents: StateFlow<List<Event>> = _hostedEvents.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch
            launch {
                eventRepository.observeMyUpcoming(uid)
                    .catch { emit(emptyList()) }
                    .collect { _upcomingEvents.value = it }
            }
            launch {
                eventRepository.observeMyPast(uid)
                    .catch { emit(emptyList()) }
                    .collect { _pastEvents.value = it }
            }
            launch {
                eventRepository.observeMyHosted(uid)
                    .catch { emit(emptyList()) }
                    .collect { _hostedEvents.value = it }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Accounts
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    val profile = userRepository.observeCurrentUser()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _state = MutableStateFlow(AccountsUiState())
    val state: StateFlow<AccountsUiState> = _state.asStateFlow()

    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepository.currentUid()
            val isAnonymous = authRepository.isAnonymous()

            _state.update {
                it.copy(
                    uid = uid.orEmpty(),
                    isAnonymous = isAnonymous
                )
            }

            if (uid == null) return@launch

            launch {
                eventRepository.observeMyUpcoming(uid)
                    .catch { emit(emptyList()) }
                    .collect { events ->
                        _state.update { current ->
                            current.copy(upcomingCount = events.size)
                        }
                    }
            }
            launch {
                eventRepository.observeMyPast(uid)
                    .catch { emit(emptyList()) }
                    .collect { events ->
                        _state.update { current ->
                            current.copy(pastCount = events.size)
                        }
                    }
            }
            launch {
                eventRepository.observeMyHosted(uid)
                    .catch { emit(emptyList()) }
                    .collect { events ->
                        _state.update { current ->
                            current.copy(hostedCount = events.size)
                        }
                    }
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            _isSigningOut.value = true
            authRepository.signOut()
            _isSigningOut.value = false
            onSignedOut()
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Host Controls
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class HostControlsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    val event = eventRepository.observeEvent(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val attendees = eventRepository.observeEventAttendees(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hostUid = MutableStateFlow<String?>(null)

    val isHost = combine(event, _hostUid) { e, uid ->
        e?.hostUid?.isNotBlank() == true && e.hostUid == uid
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _kickingUid = MutableStateFlow<String?>(null)
    val kickingUid: StateFlow<String?> = _kickingUid.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    init {
        viewModelScope.launch {
            _hostUid.value = authRepository.currentUid()
        }
    }

    fun kickAttendee(attendee: EventAttendee) {
        if (_kickingUid.value != null) return
        if (attendee.uid.isBlank()) return

        viewModelScope.launch {
            _kickingUid.value = attendee.uid
            val result = eventRepository.removeAttendee(eventId, attendee.uid)
            _status.value = if (result.isSuccess) {
                "${attendee.name.ifBlank { "Attendee" }} removed"
            } else {
                result.exceptionOrNull()?.message ?: "Could not remove attendee"
            }
            _kickingUid.value = null

            kotlinx.coroutines.delay(2500)
            _status.value = null
        }
    }

    fun checkInAttendee(uid: String) {
        viewModelScope.launch {
            val result = eventRepository.markAttendeeCheckedIn(eventId, uid)
            _status.value = if (result.isSuccess) "Checked in" else result.exceptionOrNull()?.message
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Recap Wall
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class RecapWallViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val aiAssistRepository: AiAssistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    val posts = eventRepository.observeRecapWall(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiBusy = MutableStateFlow(false)
    val aiBusy: StateFlow<Boolean> = _aiBusy.asStateFlow()

    fun post(caption: String, imageUrl: String) {
        viewModelScope.launch {
            eventRepository.postRecap(eventId, caption, imageUrl)
        }
    }

    fun polishCaption(caption: String, onResult: (String) -> Unit) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            _aiBusy.value = true
            val polished = aiAssistRepository.polishRecapCaption(caption).getOrDefault(caption)
            onResult(polished)
            _aiBusy.value = false
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Settings
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.onboardingDone().collect { onboardingDone ->
                _state.update { it.copy(onboardingDone = onboardingDone) }
            }
        }

        viewModelScope.launch {
            settingsRepository.themeMode().collect { themeMode ->
                _state.update { it.copy(themeMode = themeMode) }
            }
        }

        viewModelScope.launch {
            settingsRepository.aiSettings().collect { settings ->
                _state.update {
                    it.copy(
                        provider = settings.provider,
                        openAiApiKey = settings.openAiApiKey,
                        geminiApiKey = settings.geminiApiKey,
                        model = settings.model
                    )
                }
            }
        }
    }

    fun setOnboardingDone(done: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(done)
            _state.update { it.copy(onboardingDone = done) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _state.update { it.copy(themeMode = mode) }
        }
    }

    fun setProvider(provider: AiProvider) {
        _state.update { it.copy(provider = provider) }
    }

    fun setOpenAiKey(value: String) {
        _state.update { it.copy(openAiApiKey = value) }
    }

    fun setGeminiKey(value: String) {
        _state.update { it.copy(geminiApiKey = value) }
    }

    fun setModel(value: String) {
        _state.update { it.copy(model = value) }
    }

    fun save(onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            val current = _state.value
            settingsRepository.saveAiSettings(
                AiSettings(
                    provider = current.provider,
                    openAiApiKey = current.openAiApiKey.trim(),
                    geminiApiKey = current.geminiApiKey.trim(),
                    model = current.model.trim()
                )
            )
            _state.update { it.copy(status = "Saved") }
            onSaved?.invoke()

            kotlinx.coroutines.delay(3000)
            _state.update { it.copy(status = null) }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Notifications
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepository.currentUid()
            if (uid != null) {
                eventRepository.observeMyUpcoming(uid).collect { events ->
                    val alerts = events.map { event ->
                        val timeRemaining = event.dateTime - System.currentTimeMillis()
                        val hours = timeRemaining / (60 * 60 * 1000)
                        val days = hours / 24

                        when {
                            hours < 0 -> "HAPPENING NOW: ${event.title.uppercase()}"
                            hours < 1 -> "REMINDER: ${event.title.uppercase()} STARTS IN LESS THAN 1 HOUR"
                            hours < 24 -> "REMINDER: ${event.title.uppercase()} STARTS IN $hours HOURS"
                            else -> "REMINDER: ${event.title.uppercase()} STARTS IN $days DAYS"
                        }
                    }
                    _notifications.value = if (alerts.isEmpty()) {
                        listOf("NO UPCOMING SIGNALS. DISCOVER NEW EVENTS TO STAY UPDATED.")
                    } else {
                        alerts
                    }
                }
            } else {
                _notifications.value = listOf("PLEASE SIGN IN TO VIEW YOUR SIGNAL FEED.")
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Poster Scan (ML Kit AI)
// ────────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class PosterScanViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _analyzing = MutableStateFlow(false)
    val analyzing: StateFlow<Boolean> = _analyzing.asStateFlow()

    private val _matchedEvents = MutableStateFlow<List<Event>>(emptyList())
    val matchedEvents: StateFlow<List<Event>> = _matchedEvents.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    fun analyzePoster(imageProxy: ImageProxy) {
        if (_analyzing.value) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        _analyzing.value = true
        _status.value = "Running AI text extraction..."
        _matchedEvents.value = emptyList()

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text.lowercase()
                matchEventsToText(rawText)
            }
            .addOnFailureListener { e ->
                _status.value = "Analysis failed: ${e.localizedMessage}"
                _analyzing.value = false
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun matchEventsToText(rawText: String) {
        viewModelScope.launch {
            _status.value = "Cross-referencing registry..."

            eventRepository.observeFeaturedAndFeed(emptyList()).collect { allEvents ->
                val activeEvents = allEvents.filter { it.status == EventStatus.LIVE }

                val scoredEvents = activeEvents.mapNotNull { event ->
                    var score = 0
                    val titleWords = event.title.lowercase().split(" ").filter { it.length > 3 }

                    if (rawText.contains(event.title.lowercase())) score += 50
                    titleWords.forEach { if (rawText.contains(it)) score += 10 }

                    if (event.hostName.isNotBlank() && rawText.contains(event.hostName.lowercase())) score += 20
                    if (event.venue.isNotBlank() && rawText.contains(event.venue.lowercase())) score += 15

                    if (score > 15) Pair(event, score) else null
                }.sortedByDescending { it.second }.map { it.first }

                if (scoredEvents.isEmpty()) {
                    _status.value = "No matching events found in the registry."
                } else {
                    _status.value = "Matches found!"
                    _matchedEvents.value = scoredEvents
                }

                _analyzing.value = false
            }
        }
    }

    fun reset() {
        _matchedEvents.value = emptyList()
        _status.value = null
        _analyzing.value = false
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// UI State Data Classes
// ────────────────────────────────────────────────────────────────────────────────
data class DiscoverUiState(
    val leaderboard: List<DepartmentScore> = emptyList(),
    val events: List<Event> = emptyList()
)

data class RecapWallUiState(
    val posts: List<RecapPost> = emptyList()
)

data class SettingsUiState(
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val provider: AiProvider = AiProvider.OPENAI,
    val openAiApiKey: String = "",
    val geminiApiKey: String = "",
    val model: String = "gpt-4.1-mini",
    val status: String? = null
)

data class AccountsUiState(
    val uid: String = "",
    val isAnonymous: Boolean = false,
    val upcomingCount: Int = 0,
    val pastCount: Int = 0,
    val hostedCount: Int = 0
)