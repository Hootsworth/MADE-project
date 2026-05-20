package com.afterlight.madeproject.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.repository.EventRepository
import com.afterlight.madeproject.domain.usecase.RSVPEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PaymentCheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val rsvpEventUseCase: RSVPEventUseCase
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    val event: StateFlow<Event?> = eventRepository.observeEvent(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing.asStateFlow()

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    fun completePayment() {
        if (eventId.isBlank() || _processing.value || _completed.value) return

        viewModelScope.launch {
            _processing.value = true
            kotlinx.coroutines.delay(800)
            val result = rsvpEventUseCase(eventId, null)
            _processing.value = false
            if (result.isSuccess) {
                _completed.value = true
                _status.value = "Payment approved. RSVP confirmed."
            } else {
                _status.value = result.exceptionOrNull()?.message ?: "Payment failed"
            }
        }
    }
}
