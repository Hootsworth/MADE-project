package com.afterlight.madeproject.utils

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val eventFormatter = DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a")

    fun formatEventTime(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(eventFormatter)
    }

    fun countdown(millis: Long): String {
        val now = Instant.now().toEpochMilli()
        val d = Duration.ofMillis((millis - now).coerceAtLeast(0L))
        val days = d.toDays()
        val hours = d.toHours() % 24
        val minutes = d.toMinutes() % 60
        return "${days}d ${hours}h ${minutes}m"
    }
}
