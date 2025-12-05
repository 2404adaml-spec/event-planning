package com.eventplanner.ui

import com.eventplanner.algorithms.*
import com.eventplanner.persistence.DataStore
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * Data class for displaying scheduled events in the table.
 */
data class ScheduledEventDisplay(
    val eventTitle: String,
    val venueName: String,
    val date: String,
    val time: String,
    val status: String
)

/**
 * View for scheduling events using the Scala EventScheduler algorithm.
 */
class SchedulerView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)

    private val scheduleTable = TableView<ScheduledEventDisplay>()
    private val scheduleList = FXCollections.observableArrayList<ScheduledEventDisplay>()
    private val resultArea = TextArea()

    init {
        setupUI()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Description
        val descriptionLabel = Label(
            "Create a conflict-free schedule for all pending events.\n" +
            "The algorithm uses functional programming to optimize venue and time allocation."
        ).apply {
            style = "-fx-font-style: italic; -fx-padding: 10px;"
            isWrapText = true
        }

        // Action buttons
        val buttonBox = HBox(10.0).apply {
            children.addAll(
                Button("Generate Schedule").apply {
                    setOnAction { generateSchedule() }
                    style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
                },
                Button("Clear Results").apply {
                    setOnAction { clearResults() }
                }
            )
        }

        // Table setup
        setupTable()

        // Result summary area
        resultArea.apply {
            isEditable = false
            prefRowCount = 6
            style = "-fx-font-family: monospace;"
            promptText = "Schedule summary will appear here..."
        }

        VBox.setVgrow(scheduleTable, Priority.ALWAYS)

        root.children.addAll(
            Label("Event Scheduler (Scala Algorithm)").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            descriptionLabel,
            buttonBox,
            Separator(),
            Label("Generated Schedule:").apply { style = "-fx-font-weight: bold;" },
            scheduleTable,
            Separator(),
            Label("Summary:").apply { style = "-fx-font-weight: bold;" },
            resultArea
        )
    }

    private fun setupTable() {
        val titleCol = TableColumn<ScheduledEventDisplay, String>("Event").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(cellData.value.eventTitle)
            }
            prefWidth = 200.0
        }

        val venueCol = TableColumn<ScheduledEventDisplay, String>("Venue").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(cellData.value.venueName)
            }
            prefWidth = 150.0
        }

        val dateCol = TableColumn<ScheduledEventDisplay, String>("Date").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(cellData.value.date)
            }
            prefWidth = 100.0
        }

        val timeCol = TableColumn<ScheduledEventDisplay, String>("Time").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(cellData.value.time)
            }
            prefWidth = 120.0
        }

        val statusCol = TableColumn<ScheduledEventDisplay, String>("Status").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(cellData.value.status)
            }
            prefWidth = 100.0
        }

        scheduleTable.columns.addAll(titleCol, venueCol, dateCol, timeCol, statusCol)
        scheduleTable.items = scheduleList
    }

    private fun generateSchedule() {
        try {
            scheduleList.clear()

            // Get events to schedule
            val events = dataStore.getAllEvents().map { event ->
                ScheduleEvent(
                    event.id,
                    event.title,
                    event.durationMinutes(),
                    event.maxParticipants,
                    event.date,
                    event.startTime
                )
            }

            if (events.isEmpty()) {
                resultArea.text = "No events to schedule. Please create events first."
                return
            }

            // Get venues
            val venues = dataStore.getAllVenues().map { venue ->
                VenueSlot(venue.id, venue.name, venue.capacity, venue.location)
            }

            if (venues.isEmpty()) {
                resultArea.text = "No venues available. Please add venues first."
                return
            }

            // Call Scala EventScheduler algorithm
            val result = EventScheduler.schedule(
                java.util.ArrayList(events),
                java.util.ArrayList(venues),
                java.util.ArrayList<OccupiedSlot>()
            )

            // Display scheduled events
            val scheduledEvents = result.scheduledEvents()
            for (i in 0 until scheduledEvents.size) {
                val scheduled = scheduledEvents.get(i)
                scheduleList.add(
                    ScheduledEventDisplay(
                        scheduled.eventTitle(),
                        scheduled.venueName(),
                        scheduled.date(),
                        "${scheduled.startTime()} - ${scheduled.endTime()}",
                        if (scheduled.scheduled()) "Scheduled" else "Failed"
                    )
                )
            }

            // Display unscheduled events
            val unscheduled = result.unscheduledEvents()
            for (i in 0 until unscheduled.size) {
                val eventId = unscheduled.get(i)
                val event = events.find { it.id() == eventId }
                if (event != null) {
                    scheduleList.add(
                        ScheduledEventDisplay(
                            event.title(),
                            "N/A",
                            "N/A",
                            "N/A",
                            "Unscheduled"
                        )
                    )
                }
            }

            // Display summary
            val conflicts = result.conflicts()
            resultArea.text = buildString {
                appendLine("Schedule Generation Complete")
                appendLine("=" .repeat(40))
                appendLine()
                appendLine("Total events: ${events.size}")
                appendLine("Successfully scheduled: ${scheduledEvents.size}")
                appendLine("Unscheduled: ${unscheduled.size}")
                appendLine("Conflicts detected: ${conflicts.size}")

                if (conflicts.size > 0) {
                    appendLine()
                    appendLine("Conflicts:")
                    for (i in 0 until conflicts.size) {
                        appendLine("• ${conflicts.get(i)}")
                    }
                }

                if (unscheduled.size > 0) {
                    appendLine()
                    appendLine("Some events could not be scheduled.")
                    appendLine("Consider adding more venues or adjusting event times.")
                }
            }
        } catch (e: Exception) {
            resultArea.text = "Error generating schedule: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun clearResults() {
        scheduleList.clear()
        resultArea.clear()
    }
}
