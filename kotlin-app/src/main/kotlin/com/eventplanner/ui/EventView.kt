package com.eventplanner.ui

import com.eventplanner.domain.Event
import com.eventplanner.domain.EventType
import com.eventplanner.domain.EventStatus
import com.eventplanner.persistence.DataStore
import com.eventplanner.service.ValidationUtils
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * View for creating and managing events.
 */
class EventView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)
    private val eventTable = TableView<Event>()
    private val eventList = FXCollections.observableArrayList<Event>()

    // Form fields
    private val titleField = TextField()
    private val descriptionArea = TextArea()
    private val dateField = TextField()
    private val startTimeField = TextField()
    private val endTimeField = TextField()
    private val venueCombo = ComboBox<String>()
    private val maxParticipantsField = TextField()
    private val eventTypeCombo = ComboBox<EventType>()

    init {
        setupUI()
        loadEvents()
        loadVenues()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Form section
        val formGrid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(10.0)

            add(Label("Event Title:"), 0, 0)
            add(titleField.apply { promptText = "Enter event title" }, 1, 0)

            add(Label("Description:"), 0, 1)
            add(descriptionArea.apply {
                promptText = "Enter event description"
                prefRowCount = 2
            }, 1, 1)

            add(Label("Date (YYYY-MM-DD):"), 0, 2)
            add(dateField.apply { promptText = "e.g., 2024-03-15" }, 1, 2)

            add(Label("Start Time (HH:MM):"), 0, 3)
            add(startTimeField.apply { promptText = "e.g., 09:00" }, 1, 3)

            add(Label("End Time (HH:MM):"), 0, 4)
            add(endTimeField.apply { promptText = "e.g., 17:00" }, 1, 4)

            add(Label("Venue:"), 0, 5)
            add(venueCombo.apply { prefWidth = 200.0 }, 1, 5)

            add(Label("Max Participants:"), 0, 6)
            add(maxParticipantsField.apply { promptText = "Enter max participants" }, 1, 6)

            add(Label("Event Type:"), 0, 7)
            add(eventTypeCombo.apply {
                items = FXCollections.observableArrayList(*EventType.values())
                selectionModel.selectFirst()
            }, 1, 7)
        }

        // Buttons
        val buttonBox = HBox(10.0).apply {
            children.addAll(
                Button("Create Event").apply {
                    setOnAction { createEvent() }
                    style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                },
                Button("Update Selected").apply {
                    setOnAction { updateEvent() }
                },
                Button("Delete Selected").apply {
                    setOnAction { deleteEvent() }
                    style = "-fx-background-color: #f44336; -fx-text-fill: white;"
                },
                Button("Clear Form").apply {
                    setOnAction { clearForm() }
                },
                Button("Refresh Venues").apply {
                    setOnAction { loadVenues() }
                }
            )
        }

        // Table setup
        setupTable()

        // Selection listener
        eventTable.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            selected?.let { populateForm(it) }
        }

        VBox.setVgrow(eventTable, Priority.ALWAYS)

        root.children.addAll(
            Label("Event Management").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            formGrid,
            buttonBox,
            Separator(),
            eventTable
        )
    }

    private fun setupTable() {
        val titleCol = TableColumn<Event, String>("Title").apply {
            cellValueFactory = PropertyValueFactory("title")
            prefWidth = 150.0
        }

        val dateCol = TableColumn<Event, String>("Date").apply {
            cellValueFactory = PropertyValueFactory("date")
            prefWidth = 100.0
        }

        val timeCol = TableColumn<Event, String>("Time").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(
                    "${cellData.value.startTime}-${cellData.value.endTime}"
                )
            }
            prefWidth = 100.0
        }

        val typeCol = TableColumn<Event, EventType>("Type").apply {
            cellValueFactory = PropertyValueFactory("eventType")
            prefWidth = 100.0
        }

        val capacityCol = TableColumn<Event, String>("Capacity").apply {
            setCellValueFactory { cellData ->
                val event = cellData.value
                javafx.beans.property.SimpleStringProperty(
                    "${event.registeredParticipantIds.size}/${event.maxParticipants}"
                )
            }
            prefWidth = 80.0
        }

        val statusCol = TableColumn<Event, EventStatus>("Status").apply {
            cellValueFactory = PropertyValueFactory("status")
            prefWidth = 100.0
        }

        eventTable.columns.addAll(titleCol, dateCol, timeCol, typeCol, capacityCol, statusCol)
        eventTable.items = eventList
    }

    private fun loadEvents() {
        eventList.clear()
        eventList.addAll(dataStore.getAllEvents())
    }

    private fun loadVenues() {
        val venues = dataStore.getAllVenues()
        venueCombo.items = FXCollections.observableArrayList(
            venues.map { "${it.id}|${it.name} (${it.capacity})" }
        )
        if (venues.isNotEmpty()) {
            venueCombo.selectionModel.selectFirst()
        }
    }

    private fun createEvent() {
        try {
            // Validate all inputs
            val title = ValidationUtils.validateRequired(titleField.text, "Event title")
            val date = ValidationUtils.formatDate(dateField.text.trim())
            val startTime = ValidationUtils.formatTime(startTimeField.text.trim())
            val endTime = ValidationUtils.formatTime(endTimeField.text.trim())
            ValidationUtils.validateTimeRange(startTime, endTime)
            val maxParticipants = ValidationUtils.validatePositiveInt(
                maxParticipantsField.text, "Max participants"
            )

            val venueSelection = venueCombo.value?.split("|")?.get(0)
                ?: throw IllegalArgumentException("Please select a venue")

            val event = Event(
                title = title,
                description = descriptionArea.text.trim(),
                date = date,
                startTime = startTime,
                endTime = endTime,
                venueId = venueSelection,
                maxParticipants = maxParticipants,
                eventType = eventTypeCombo.value
            )

            dataStore.addEvent(event)
            loadEvents()
            clearForm()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event created successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create event: ${e.message}")
        }
    }

    private fun updateEvent() {
        val selected = eventTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an event to update")
            return
        }

        try {
            // Validate all inputs
            val title = ValidationUtils.validateRequired(titleField.text, "Event title")
            val date = ValidationUtils.formatDate(dateField.text.trim())
            val startTime = ValidationUtils.formatTime(startTimeField.text.trim())
            val endTime = ValidationUtils.formatTime(endTimeField.text.trim())
            ValidationUtils.validateTimeRange(startTime, endTime)
            val maxParticipants = ValidationUtils.validatePositiveInt(
                maxParticipantsField.text, "Max participants"
            )

            val venueSelection = venueCombo.value?.split("|")?.get(0)
                ?: throw IllegalArgumentException("Please select a venue")

            val updated = selected.copy(
                title = title,
                description = descriptionArea.text.trim(),
                date = date,
                startTime = startTime,
                endTime = endTime,
                venueId = venueSelection,
                maxParticipants = maxParticipants,
                eventType = eventTypeCombo.value
            )

            dataStore.updateEvent(updated)
            loadEvents()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event: ${e.message}")
        }
    }

    private fun deleteEvent() {
        val selected = eventTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an event to delete")
            return
        }

        val confirm = Alert(Alert.AlertType.CONFIRMATION, "Delete event '${selected.title}'?").showAndWait()
        if (confirm.isPresent && confirm.get() == ButtonType.OK) {
            dataStore.deleteEvent(selected.id)
            loadEvents()
            clearForm()
        }
    }

    private fun populateForm(event: Event) {
        titleField.text = event.title
        descriptionArea.text = event.description
        dateField.text = event.date
        startTimeField.text = event.startTime
        endTimeField.text = event.endTime
        maxParticipantsField.text = event.maxParticipants.toString()
        eventTypeCombo.value = event.eventType

        // Select venue in combo
        val venueItem = venueCombo.items.find { it.startsWith(event.venueId) }
        venueCombo.value = venueItem
    }

    private fun clearForm() {
        titleField.clear()
        descriptionArea.clear()
        dateField.clear()
        startTimeField.clear()
        endTimeField.clear()
        maxParticipantsField.clear()
        eventTypeCombo.selectionModel.selectFirst()
        if (venueCombo.items.isNotEmpty()) {
            venueCombo.selectionModel.selectFirst()
        }
        eventTable.selectionModel.clearSelection()
    }

    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }
}
