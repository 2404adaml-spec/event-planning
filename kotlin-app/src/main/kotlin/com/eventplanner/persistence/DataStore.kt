package com.eventplanner.persistence

import com.eventplanner.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Container for all application data that will be persisted.
 */
@Serializable
data class AppData(
    val events: MutableList<Event> = mutableListOf(),
    val venues: MutableList<Venue> = mutableListOf(),
    val participants: MutableList<Participant> = mutableListOf(),
    val sessions: MutableList<Session> = mutableListOf()
)

/**
 * Handles persistence of application data to JSON files.
 * Provides methods for saving and loading all domain objects.
 */
class DataStore(private val dataFilePath: String = "event_planner_data.json") {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var appData: AppData = AppData()

    init {
        loadData()
    }

    /**
     * Loads data from the JSON file.
     */
    fun loadData() {
        val file = File(dataFilePath)
        if (file.exists()) {
            try {
                val jsonString = file.readText()
                appData = json.decodeFromString<AppData>(jsonString)
            } catch (e: Exception) {
                println("Error loading data: ${e.message}")
                appData = AppData()
            }
        }
    }

    /**
     * Saves all data to the JSON file.
     */
    fun saveData() {
        try {
            val jsonString = json.encodeToString(appData)
            File(dataFilePath).writeText(jsonString)
        } catch (e: Exception) {
            println("Error saving data: ${e.message}")
            throw e
        }
    }

    // Event operations
    fun getAllEvents(): List<Event> = appData.events.toList()

    fun getEventById(id: String): Event? = appData.events.find { it.id == id }

    fun addEvent(event: Event) {
        appData.events.add(event)
        saveData()
    }

    fun updateEvent(event: Event) {
        val index = appData.events.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            appData.events[index] = event
            saveData()
        }
    }

    fun deleteEvent(eventId: String) {
        appData.events.removeIf { it.id == eventId }
        appData.sessions.removeIf { it.eventId == eventId }
        saveData()
    }

    // Venue operations
    fun getAllVenues(): List<Venue> = appData.venues.toList()

    fun getVenueById(id: String): Venue? = appData.venues.find { it.id == id }

    fun addVenue(venue: Venue) {
        appData.venues.add(venue)
        saveData()
    }

    fun updateVenue(venue: Venue) {
        val index = appData.venues.indexOfFirst { it.id == venue.id }
        if (index >= 0) {
            appData.venues[index] = venue
            saveData()
        }
    }

    fun deleteVenue(venueId: String) {
        appData.venues.removeIf { it.id == venueId }
        saveData()
    }

    // Participant operations
    fun getAllParticipants(): List<Participant> = appData.participants.toList()

    fun getParticipantById(id: String): Participant? = appData.participants.find { it.id == id }

    fun addParticipant(participant: Participant) {
        appData.participants.add(participant)
        saveData()
    }

    fun updateParticipant(participant: Participant) {
        val index = appData.participants.indexOfFirst { it.id == participant.id }
        if (index >= 0) {
            appData.participants[index] = participant
            saveData()
        }
    }

    fun deleteParticipant(participantId: String) {
        appData.participants.removeIf { it.id == participantId }
        // Also remove from all event registrations
        appData.events.forEach { event ->
            event.registeredParticipantIds.remove(participantId)
        }
        saveData()
    }

    // Session operations
    fun getAllSessions(): List<Session> = appData.sessions.toList()

    fun getSessionsByEventId(eventId: String): List<Session> =
        appData.sessions.filter { it.eventId == eventId }

    fun addSession(session: Session) {
        appData.sessions.add(session)
        saveData()
    }

    fun updateSession(session: Session) {
        val index = appData.sessions.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            appData.sessions[index] = session
            saveData()
        }
    }

    fun deleteSession(sessionId: String) {
        appData.sessions.removeIf { it.id == sessionId }
        saveData()
    }

    // Participant registration operations
    fun registerParticipantForEvent(participantId: String, eventId: String): Boolean {
        val event = getEventById(eventId) ?: return false
        val participant = getParticipantById(participantId) ?: return false

        if (!event.hasAvailableCapacity()) return false
        if (event.isParticipantRegistered(participantId)) return false

        event.registerParticipant(participantId)
        saveData()
        return true
    }

    fun unregisterParticipantFromEvent(participantId: String, eventId: String): Boolean {
        val event = getEventById(eventId) ?: return false
        val result = event.unregisterParticipant(participantId)
        if (result) saveData()
        return result
    }

    fun getParticipantsForEvent(eventId: String): List<Participant> {
        val event = getEventById(eventId) ?: return emptyList()
        return event.registeredParticipantIds.mapNotNull { getParticipantById(it) }
    }

    fun getEventsForParticipant(participantId: String): List<Event> {
        return appData.events.filter { it.isParticipantRegistered(participantId) }
    }

    /**
     * Clears all data (useful for testing).
     */
    fun clearAllData() {
        appData = AppData()
        saveData()
    }
}
