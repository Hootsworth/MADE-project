package com.afterlight.madeproject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterlight.madeproject.domain.repository.EventRepository
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class QrCheckInViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _scanned = MutableStateFlow("")
    val scanned: StateFlow<String> = _scanned

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun onScanned(value: String): Boolean {
        _scanned.value = value.trim()
        val (eventId, ticket) = parseEventAndTicket(_scanned.value)
        return eventId.isNotBlank() && ticket.isNotBlank()
    }

    private fun parseEventAndTicket(value: String): Pair<String, String> {
        val trimmed = value.trim()
        val marker = "/events/"
        val eventId = trimmed.substringAfter(marker, missingDelimiterValue = "").substringBefore("?")
            .substringBefore("#").trim('/').ifBlank { "" }
        val ticket = trimmed.substringAfter("ticket=", missingDelimiterValue = "").substringBefore("&").trim()
        return Pair(eventId, ticket.ifBlank { trimmed })
    }

    fun checkInScanned() {
        val value = _scanned.value
        if (value.isBlank()) {
            _status.value = "Scan or paste a ticket code first"
            return
        }
        val (eventId, ticket) = parseEventAndTicket(value)
        if (eventId.isBlank() || ticket.isBlank()) {
            _status.value = "Could not parse event or ticket from input"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            val result = eventRepository.checkInWithTicket(eventId, ticket)
            _loading.value = false
            _status.value = if (result.isSuccess) "Checked in" else result.exceptionOrNull()?.message ?: "Check-in failed"
        }
    }
}
