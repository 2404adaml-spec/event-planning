package com.eventplanner.domain

import kotlinx.serialization.Serializable
import java.util.UUID

// Venue where events can be held
@Serializable
data class Venue(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val capacity: Int,
    val location: String,
    val facilities: List<String> = emptyList(),
    val hourlyRate: Double = 0.0,
    val isAvailable: Boolean = true
) {
    init {
        // validation
        require(name.isNotBlank()) { "Venue name cannot be blank" }
        require(capacity > 0) { "Venue capacity must be positive" }
        require(location.isNotBlank()) { "Venue location cannot be blank" }
        require(hourlyRate >= 0) { "Hourly rate cannot be negative" }
    }

    // check if venue is big enough
    fun canAccommodate(participants: Int): Boolean = participants <= capacity

    // format for display
    fun displayString(): String = "$name ($location) - Capacity: $capacity"
}
