package com.eventplanner.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit tests for ValidationUtils.
 */
class ValidationUtilsTest {

    @Test
    fun `should validate correct date format`() {
        val result = ValidationUtils.validateDate("2024-03-15")
        assertEquals(2024, result.year)
        assertEquals(3, result.monthValue)
        assertEquals(15, result.dayOfMonth)
    }

    @Test
    fun `should reject invalid date format`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateDate("15-03-2024")
        }
    }

    @Test
    fun `should reject blank date`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateDate("")
        }
    }

    @Test
    fun `should validate correct time format`() {
        val result = ValidationUtils.validateTime("09:30")
        assertEquals(9, result.hour)
        assertEquals(30, result.minute)
    }

    @Test
    fun `should reject invalid time format`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateTime("9:30 AM")
        }
    }

    @Test
    fun `should reject out of range time`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateTime("25:00")
        }
    }

    @Test
    fun `should validate time range correctly`() {
        // Should not throw for valid range
        ValidationUtils.validateTimeRange("09:00", "17:00")
    }

    @Test
    fun `should reject invalid time range`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateTimeRange("17:00", "09:00")
        }
    }

    @Test
    fun `should reject same start and end time`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateTimeRange("09:00", "09:00")
        }
    }

    @Test
    fun `should validate positive integer`() {
        assertEquals(50, ValidationUtils.validatePositiveInt("50", "Test"))
    }

    @Test
    fun `should reject zero as positive integer`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validatePositiveInt("0", "Test")
        }
    }

    @Test
    fun `should reject negative as positive integer`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validatePositiveInt("-5", "Test")
        }
    }

    @Test
    fun `should reject non-numeric string as integer`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validatePositiveInt("abc", "Test")
        }
    }

    @Test
    fun `should validate non-negative double`() {
        assertEquals(50.5, ValidationUtils.validateNonNegativeDouble("50.5", "Test"))
    }

    @Test
    fun `should accept zero as non-negative double`() {
        assertEquals(0.0, ValidationUtils.validateNonNegativeDouble("0", "Test"))
    }

    @Test
    fun `should reject negative double`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateNonNegativeDouble("-10.5", "Test")
        }
    }

    @Test
    fun `should validate correct email format`() {
        assertEquals("test@example.com", ValidationUtils.validateEmail("test@example.com"))
    }

    @Test
    fun `should reject email without at symbol`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateEmail("testexample.com")
        }
    }

    @Test
    fun `should reject email without domain`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateEmail("test@")
        }
    }

    @Test
    fun `should validate required field`() {
        assertEquals("test value", ValidationUtils.validateRequired("test value", "Test"))
    }

    @Test
    fun `should reject blank required field`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateRequired("", "Test")
        }
    }

    @Test
    fun `should reject null required field`() {
        assertThrows<IllegalArgumentException> {
            ValidationUtils.validateRequired(null, "Test")
        }
    }

    @Test
    fun `should format time correctly`() {
        assertEquals("09:00", ValidationUtils.formatTime("9:00"))
        assertEquals("14:30", ValidationUtils.formatTime("14:30"))
    }

    @Test
    fun `should format date correctly`() {
        assertEquals("2024-03-05", ValidationUtils.formatDate("2024-3-5"))
    }
}
