package com.eventplanner.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for Event domain class.
 */
class EventTest {

    @Test
    fun `should create event with valid data`() {
        val event = Event(
            title = "Tech Conference",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "17:00",
            venueId = "venue-1",
            maxParticipants = 100
        )

        assertEquals("Tech Conference", event.title)
        assertEquals("2024-03-15", event.date)
        assertEquals(100, event.maxParticipants)
        assertEquals(EventStatus.SCHEDULED, event.status)
    }

    @Test
    fun `should throw exception for blank title`() {
        assertThrows<IllegalArgumentException> {
            Event(
                title = "",
                date = "2024-03-15",
                startTime = "09:00",
                endTime = "17:00",
                venueId = "venue-1",
                maxParticipants = 100
            )
        }
    }

    @Test
    fun `should throw exception for zero max participants`() {
        assertThrows<IllegalArgumentException> {
            Event(
                title = "Test Event",
                date = "2024-03-15",
                startTime = "09:00",
                endTime = "17:00",
                venueId = "venue-1",
                maxParticipants = 0
            )
        }
    }

    @Test
    fun `should calculate available slots correctly`() {
        val event = Event(
            title = "Workshop",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "12:00",
            venueId = "venue-1",
            maxParticipants = 50
        )

        assertEquals(50, event.availableSlots())

        event.registerParticipant("participant-1")
        assertEquals(49, event.availableSlots())

        event.registerParticipant("participant-2")
        assertEquals(48, event.availableSlots())
    }

    @Test
    fun `should register participant successfully`() {
        val event = Event(
            title = "Workshop",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "12:00",
            venueId = "venue-1",
            maxParticipants = 2
        )

        assertTrue(event.registerParticipant("participant-1"))
        assertTrue(event.isParticipantRegistered("participant-1"))
        assertEquals(1, event.availableSlots())
    }

    @Test
    fun `should not register same participant twice`() {
        val event = Event(
            title = "Workshop",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "12:00",
            venueId = "venue-1",
            maxParticipants = 10
        )

        assertTrue(event.registerParticipant("participant-1"))
        assertFalse(event.registerParticipant("participant-1"))
    }

    @Test
    fun `should not exceed capacity`() {
        val event = Event(
            title = "Small Meeting",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "10:00",
            venueId = "venue-1",
            maxParticipants = 2
        )

        assertTrue(event.registerParticipant("participant-1"))
        assertTrue(event.registerParticipant("participant-2"))
        assertFalse(event.registerParticipant("participant-3"))
        assertFalse(event.hasAvailableCapacity())
    }

    @Test
    fun `should unregister participant successfully`() {
        val event = Event(
            title = "Workshop",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "12:00",
            venueId = "venue-1",
            maxParticipants = 10
        )

        event.registerParticipant("participant-1")
        assertTrue(event.unregisterParticipant("participant-1"))
        assertFalse(event.isParticipantRegistered("participant-1"))
    }

    @Test
    fun `should calculate duration correctly`() {
        val event = Event(
            title = "Workshop",
            date = "2024-03-15",
            startTime = "09:00",
            endTime = "11:30",
            venueId = "venue-1",
            maxParticipants = 10
        )

        assertEquals(150, event.durationMinutes()) // 2.5 hours = 150 minutes
    }

    @Test
    fun `should create display string correctly`() {
        val event = Event(
            title = "Annual Meeting",
            date = "2024-03-15",
            startTime = "14:00",
            endTime = "16:00",
            venueId = "venue-1",
            maxParticipants = 50
        )

        assertEquals("Annual Meeting - 2024-03-15 14:00-16:00", event.displayString())
    }
}
