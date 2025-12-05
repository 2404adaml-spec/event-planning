package com.eventplanner.domain

import kotlinx.serialization.Serializable
import java.util.UUID

// Store participant information
@Serializable
data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String = "",
    val organization: String = ""
) {
    init {
        // check required fields
        require(name.isNotBlank()) { "Participant name cannot be blank" }
        require(email.isNotBlank()) { "Participant email cannot be blank" }
        require(isValidEmail(email)) { "Invalid email format" }
    }

    // simple email check
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    // format for display
    fun displayString(): String = "$name ($email)"
}
