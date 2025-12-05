package com.eventplanner.domain

import kotlinx.serialization.Serializable
import java.util.UUID

// Class to store event information
@Serializable
data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val date: String,  // YYYY-MM-DD format
    val startTime: String,  // HH:MM format
    val endTime: String,  // HH:MM format
    val venueId: String,
    val maxParticipants: Int,
    val registeredParticipantIds: MutableList<String> = mutableListOf(),
    val eventType: EventType = EventType.WORKSHOP,
    val status: EventStatus = EventStatus.SCHEDULED
) {
    init {
        // basic validation
        require(title.isNotBlank()) { "Event title cannot be blank" }
        require(date.isNotBlank()) { "Event date cannot be blank" }
        require(startTime.isNotBlank()) { "Start time cannot be blank" }
        require(endTime.isNotBlank()) { "End time cannot be blank" }
        require(maxParticipants > 0) { "Max participants must be positive" }
    }

    // Calculate how many slots are left
    fun availableSlots(): Int = maxParticipants - registeredParticipantIds.size

    // Check if there's room for more participants
    fun hasAvailableCapacity(): Boolean = availableSlots() > 0

    // Check if someone is already registered
    fun isParticipantRegistered(participantId: String): Boolean =
        participantId in registeredParticipantIds

    // Register a participant to the event
    fun registerParticipant(participantId: String): Boolean {
        if (!hasAvailableCapacity()) return false  // event is full
        if (isParticipantRegistered(participantId)) return false  // already registered
        registeredParticipantIds.add(participantId)
        return true
    }

    // Remove a participant from the event
    fun unregisterParticipant(participantId: String): Boolean {
        return registeredParticipantIds.remove(participantId)
    }

    // Format event for display
    fun displayString(): String = "$title - $date $startTime-$endTime"

    // Calculate event duration in minutes
    fun durationMinutes(): Int {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
        return endMinutes - startMinutes
    }
}

// Different types of events
@Serializable
enum class EventType {
    WORKSHOP,
    CONFERENCE,
    SEMINAR,
    MEETING,
    FESTIVAL,
    TRAINING,
    NETWORKING
}

// Event status values
@Serializable
enum class EventStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
