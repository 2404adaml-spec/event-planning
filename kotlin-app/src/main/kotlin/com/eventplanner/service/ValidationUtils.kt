package com.eventplanner.service

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Helper class for checking if inputs are valid
object ValidationUtils {

    // Check if date is valid
    fun validateDate(date: String, fieldName: String = "Date"): LocalDate {
        // check if empty
        if (date.isBlank()) {
            throw IllegalArgumentException("$fieldName is required")
        }

        // try to parse the date
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
            return LocalDate.parse(date, formatter)
        } catch (e: Exception) {
            throw IllegalArgumentException("$fieldName must be in YYYY-MM-DD format")
        }
    }

    // Check if time is valid
    fun validateTime(time: String, fieldName: String = "Time"): LocalTime {
        if (time.isBlank()) {
            throw IllegalArgumentException("$fieldName is required")
        }

        try {
            val formatter = DateTimeFormatter.ofPattern("H:mm")
            return LocalTime.parse(time, formatter)
        } catch (e: Exception) {
            throw IllegalArgumentException("$fieldName must be in HH:MM format")
        }
    }

    // Make sure end time is after start time
    fun validateTimeRange(startTime: String, endTime: String) {
        val start = validateTime(startTime, "Start time")
        val end = validateTime(endTime, "End time")

        if (!end.isAfter(start)) {
            throw IllegalArgumentException("End time must be after start time")
        }
    }

    // Check if date is in the future
    fun validateFutureDate(date: String, fieldName: String = "Date") {
        val parsedDate = validateDate(date, fieldName)
        if (parsedDate.isBefore(LocalDate.now())) {
            throw IllegalArgumentException("$fieldName cannot be in the past")
        }
    }

    // Check if number is positive
    fun validatePositiveInt(value: String?, fieldName: String = "Value"): Int {
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("$fieldName is required")
        }

        val number = value.toIntOrNull()
        if (number == null) {
            throw IllegalArgumentException("$fieldName must be a number")
        }

        if (number <= 0) {
            throw IllegalArgumentException("$fieldName must be positive")
        }

        return number
    }

    // Check if number is not negative (can be zero)
    fun validateNonNegativeDouble(value: String?, fieldName: String = "Value"): Double {
        if (value.isNullOrBlank()) {
            return 0.0  //default to zero
        }

        val number = value.toDoubleOrNull()
        if (number == null) {
            throw IllegalArgumentException("$fieldName must be a number")
        }

        if (number < 0) {
            throw IllegalArgumentException("$fieldName cannot be negative")
        }

        return number
    }

    // Simple email check - just make sure it has @ and .
    fun validateEmail(email: String, fieldName: String = "Email"): String {
        if (email.isBlank()) {
            throw IllegalArgumentException("$fieldName is required")
        }

        // basic check for email
        if (!email.contains("@") || !email.contains(".")) {
            throw IllegalArgumentException("$fieldName is not valid")
        }

        return email
    }

    // Check if required field is filled
    fun validateRequired(value: String?, fieldName: String = "Field"): String {
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("$fieldName is required")
        }
        return value.trim()
    }

    fun formatTime(time: String): String {
        val parsed = validateTime(time)
        // format as HH:MM
        val hour = parsed.hour.toString().padStart(2, '0')
        val minute = parsed.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    fun formatDate(date: String): String {
        val parsed = validateDate(date)
        // format as YYYY-MM-DD
        val year = parsed.year.toString()
        val month = parsed.monthValue.toString().padStart(2, '0')
        val day = parsed.dayOfMonth.toString().padStart(2, '0')
        return "$year-$month-$day"
    }
}
