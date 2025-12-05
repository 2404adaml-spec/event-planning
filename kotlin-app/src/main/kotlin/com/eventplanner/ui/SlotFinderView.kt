package com.eventplanner.ui

import com.eventplanner.algorithms.OccupiedSlot
import com.eventplanner.algorithms.SlotFinder
import com.eventplanner.algorithms.VenueSlot
import com.eventplanner.persistence.DataStore
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * View for finding available slots using the Scala SlotFinder algorithm.
 */
class SlotFinderView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)

    // Input fields
    private val capacityField = TextField()
    private val startDateField = TextField()
    private val durationField = TextField()
    private val preferredTimeField = TextField()

    // Result display
    private val resultArea = TextArea()

    init {
        setupUI()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Description
        val descriptionLabel = Label(
            "Find the first available venue slot based on your requirements.\n" +
            "The algorithm uses functional programming to search through venues and time slots."
        ).apply {
            style = "-fx-font-style: italic; -fx-padding: 10px;"
            isWrapText = true
        }

        // Input form
        val formGrid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(10.0)

            add(Label("Required Capacity:"), 0, 0)
            add(capacityField.apply { promptText = "e.g., 50" }, 1, 0)

            add(Label("Start Date (YYYY-MM-DD):"), 0, 1)
            add(startDateField.apply { promptText = "e.g., 2024-03-15" }, 1, 1)

            add(Label("Duration (minutes):"), 0, 2)
            add(durationField.apply { promptText = "e.g., 120" }, 1, 2)

            add(Label("Preferred Time (HH:MM):"), 0, 3)
            add(preferredTimeField.apply { promptText = "e.g., 09:00" }, 1, 3)
        }

        // Find button
        val buttonBox = HBox(10.0).apply {
            children.addAll(
                Button("Find Available Slot").apply {
                    setOnAction { findSlot() }
                    style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
                },
                Button("Clear").apply {
                    setOnAction { clearForm() }
                }
            )
        }

        // Result area
        resultArea.apply {
            isEditable = false
            prefRowCount = 10
            style = "-fx-font-family: monospace;"
            promptText = "Results will appear here..."
        }

        root.children.addAll(
            Label("Slot Finder (Scala Algorithm)").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            descriptionLabel,
            formGrid,
            buttonBox,
            Separator(),
            Label("Result:").apply { style = "-fx-font-weight: bold;" },
            resultArea
        )
    }

    private fun findSlot() {
        try {
            // Validate inputs
            val capacity = capacityField.text.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid capacity")
            val startDate = startDateField.text.trim()
            if (startDate.isEmpty()) throw IllegalArgumentException("Start date is required")
            val duration = durationField.text.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid duration")
            val preferredTime = preferredTimeField.text.trim().ifEmpty { "09:00" }

            // Get venues from data store
            val venues = dataStore.getAllVenues().map { venue ->
                VenueSlot(venue.id, venue.name, venue.capacity, venue.location)
            }

            if (venues.isEmpty()) {
                resultArea.text = "No venues available. Please add venues first."
                return
            }

            // Get existing events as occupied slots
            val occupiedSlots = dataStore.getAllEvents().map { event ->
                OccupiedSlot(event.venueId, event.date, event.startTime, event.endTime)
            }

            // Call Scala SlotFinder algorithm
            val result = SlotFinder.findSlot(
                java.util.ArrayList(venues),
                java.util.ArrayList(occupiedSlots),
                capacity,
                startDate,
                duration,
                preferredTime
            )

            // Display result
            if (result.found()) {
                resultArea.text = buildString {
                    appendLine("✓ SLOT FOUND!")
                    appendLine("=" .repeat(40))
                    appendLine()
                    appendLine("Venue: ${result.venue().name()}")
                    appendLine("Location: ${result.venue().location()}")
                    appendLine("Capacity: ${result.venue().capacity()}")
                    appendLine()
                    appendLine("Date: ${result.date()}")
                    appendLine("Time: ${result.startTime()} - ${result.endTime()}")
                    appendLine()
                    appendLine("You can now create an event with these details.")
                }
            } else {
                resultArea.text = buildString {
                    appendLine("✗ NO SLOT FOUND")
                    appendLine("=" .repeat(40))
                    appendLine()
                    appendLine("Could not find an available slot matching your criteria.")
                    appendLine()
                    appendLine("Suggestions:")
                    appendLine("• Try a smaller capacity requirement")
                    appendLine("• Try a later start date")
                    appendLine("• Try a different preferred time")
                    appendLine("• Add more venues with larger capacity")
                }
            }
        } catch (e: Exception) {
            resultArea.text = "Error: ${e.message}"
        }
    }

    private fun clearForm() {
        capacityField.clear()
        startDateField.clear()
        durationField.clear()
        preferredTimeField.clear()
        resultArea.clear()
    }
}
