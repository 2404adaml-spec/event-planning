package com.eventplanner.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Participant domain class.
 */
class ParticipantTest {

    @Test
    fun `should create participant with valid data`() {
        val participant = Participant(
            name = "John Doe",
            email = "john@example.com",
            phone = "123-456-7890",
            organization = "Tech Corp"
        )

        assertEquals("John Doe", participant.name)
        assertEquals("john@example.com", participant.email)
        assertEquals("123-456-7890", participant.phone)
        assertEquals("Tech Corp", participant.organization)
    }

    @Test
    fun `should throw exception for blank name`() {
        assertThrows<IllegalArgumentException> {
            Participant(name = "", email = "test@example.com")
        }
    }

    @Test
    fun `should throw exception for blank email`() {
        assertThrows<IllegalArgumentException> {
            Participant(name = "Test User", email = "")
        }
    }

    @Test
    fun `should throw exception for invalid email format`() {
        assertThrows<IllegalArgumentException> {
            Participant(name = "Test User", email = "invalid-email")
        }
    }

    @Test
    fun `should accept valid email formats`() {
        val participant = Participant(name = "Test User", email = "user@domain.com")
        assertEquals("user@domain.com", participant.email)
    }

    @Test
    fun `should create display string correctly`() {
        val participant = Participant(
            name = "Jane Smith",
            email = "jane@company.org"
        )

        assertEquals("Jane Smith (jane@company.org)", participant.displayString())
    }

    @Test
    fun `should create participant with minimal data`() {
        val participant = Participant(
            name = "Minimal User",
            email = "min@test.com"
        )

        assertEquals("", participant.phone)
        assertEquals("", participant.organization)
    }

    @Test
    fun `should generate unique id for each participant`() {
        val participant1 = Participant(name = "User 1", email = "user1@test.com")
        val participant2 = Participant(name = "User 2", email = "user2@test.com")

        assertTrue(participant1.id != participant2.id)
    }
}
