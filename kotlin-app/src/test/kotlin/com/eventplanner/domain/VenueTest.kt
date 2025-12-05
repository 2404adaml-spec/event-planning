package com.eventplanner.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for Venue domain class.
 */
class VenueTest {

    @Test
    fun `should create venue with valid data`() {
        val venue = Venue(
            name = "Conference Hall A",
            capacity = 100,
            location = "Building 1, Floor 2"
        )

        assertEquals("Conference Hall A", venue.name)
        assertEquals(100, venue.capacity)
        assertEquals("Building 1, Floor 2", venue.location)
        assertTrue(venue.isAvailable)
    }

    @Test
    fun `should throw exception for blank name`() {
        assertThrows<IllegalArgumentException> {
            Venue(name = "", capacity = 100, location = "Test Location")
        }
    }

    @Test
    fun `should throw exception for zero capacity`() {
        assertThrows<IllegalArgumentException> {
            Venue(name = "Test Venue", capacity = 0, location = "Test Location")
        }
    }

    @Test
    fun `should throw exception for negative capacity`() {
        assertThrows<IllegalArgumentException> {
            Venue(name = "Test Venue", capacity = -10, location = "Test Location")
        }
    }

    @Test
    fun `should throw exception for blank location`() {
        assertThrows<IllegalArgumentException> {
            Venue(name = "Test Venue", capacity = 100, location = "")
        }
    }

    @Test
    fun `should check if venue can accommodate participants`() {
        val venue = Venue(name = "Small Room", capacity = 50, location = "Building A")

        assertTrue(venue.canAccommodate(30))
        assertTrue(venue.canAccommodate(50))
        assertFalse(venue.canAccommodate(51))
        assertFalse(venue.canAccommodate(100))
    }

    @Test
    fun `should create display string correctly`() {
        val venue = Venue(
            name = "Main Hall",
            capacity = 200,
            location = "Downtown Center"
        )

        assertEquals("Main Hall (Downtown Center) - Capacity: 200", venue.displayString())
    }

    @Test
    fun `should create venue with facilities`() {
        val venue = Venue(
            name = "Tech Room",
            capacity = 30,
            location = "IT Building",
            facilities = listOf("Projector", "WiFi", "Whiteboard")
        )

        assertEquals(3, venue.facilities.size)
        assertTrue(venue.facilities.contains("Projector"))
    }

    @Test
    fun `should throw exception for negative hourly rate`() {
        assertThrows<IllegalArgumentException> {
            Venue(
                name = "Test Venue",
                capacity = 100,
                location = "Test Location",
                hourlyRate = -50.0
            )
        }
    }
}
