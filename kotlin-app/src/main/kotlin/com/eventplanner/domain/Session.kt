package com.eventplanner.domain

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a session within an event (for multi-session events like conferences).
 */
@Serializable
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val title: String,
    val speaker: String = "",
    val description: String = "",
    val startTime: String,  // Format: HH:MM
    val endTime: String,    // Format: HH:MM
    val room: String = ""
) {
    init {
        require(title.isNotBlank()) { "Session title cannot be blank" }
        require(startTime.isNotBlank()) { "Start time cannot be blank" }
        require(endTime.isNotBlank()) { "End time cannot be blank" }
    }

    /**
     * Returns a string representation for display purposes.
     */
    fun displayString(): String = "$title ($startTime-$endTime)"

    /**
     * Gets the duration of the session in minutes.
     */
    fun durationMinutes(): Int {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
        return endMinutes - startMinutes
    }
}
