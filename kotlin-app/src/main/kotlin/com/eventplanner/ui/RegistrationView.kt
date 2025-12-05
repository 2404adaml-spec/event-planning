package com.eventplanner.ui

import com.eventplanner.domain.Event
import com.eventplanner.domain.Participant
import com.eventplanner.persistence.DataStore
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * View for registering participants to events.
 */
class RegistrationView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)

    private val eventCombo = ComboBox<String>()
    private val participantCombo = ComboBox<String>()
    private val registeredTable = TableView<Participant>()
    private val registeredList = FXCollections.observableArrayList<Participant>()

    init {
        setupUI()
        loadData()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Event selection
        val eventBox = HBox(10.0).apply {
            children.addAll(
                Label("Select Event:"),
                eventCombo.apply {
                    prefWidth = 300.0
                    setOnAction { loadRegisteredParticipants() }
                },
                Button("Refresh").apply {
                    setOnAction { loadData() }
                }
            )
        }

        // Event info display
        val eventInfoLabel = Label().apply {
            style = "-fx-font-style: italic; -fx-padding: 5px;"
        }

        eventCombo.setOnAction {
            val selectedEvent = getSelectedEvent()
            if (selectedEvent != null) {
                eventInfoLabel.text = "Capacity: ${selectedEvent.registeredParticipantIds.size}/${selectedEvent.maxParticipants} | " +
                        "Available slots: ${selectedEvent.availableSlots()}"
            } else {
                eventInfoLabel.text = ""
            }
            loadRegisteredParticipants()
        }

        // Participant registration
        val registerBox = HBox(10.0).apply {
            children.addAll(
                Label("Register Participant:"),
                participantCombo.apply { prefWidth = 250.0 },
                Button("Register").apply {
                    setOnAction { registerParticipant() }
                    style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                },
                Button("Unregister Selected").apply {
                    setOnAction { unregisterParticipant() }
                    style = "-fx-background-color: #f44336; -fx-text-fill: white;"
                }
            )
        }

        // Table for registered participants
        setupTable()

        VBox.setVgrow(registeredTable, Priority.ALWAYS)

        root.children.addAll(
            Label("Participant Registration").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            eventBox,
            eventInfoLabel,
            Separator(),
            registerBox,
            Separator(),
            Label("Registered Participants:").apply {
                style = "-fx-font-weight: bold;"
            },
            registeredTable
        )
    }

    private fun setupTable() {
        val nameCol = TableColumn<Participant, String>("Name").apply {
            cellValueFactory = PropertyValueFactory("name")
            prefWidth = 150.0
        }

        val emailCol = TableColumn<Participant, String>("Email").apply {
            cellValueFactory = PropertyValueFactory("email")
            prefWidth = 200.0
        }

        val phoneCol = TableColumn<Participant, String>("Phone").apply {
            cellValueFactory = PropertyValueFactory("phone")
            prefWidth = 120.0
        }

        val orgCol = TableColumn<Participant, String>("Organization").apply {
            cellValueFactory = PropertyValueFactory("organization")
            prefWidth = 150.0
        }

        registeredTable.columns.addAll(nameCol, emailCol, phoneCol, orgCol)
        registeredTable.items = registeredList
    }

    private fun loadData() {
        // Load events
        val events = dataStore.getAllEvents()
        eventCombo.items = FXCollections.observableArrayList(
            events.map { "${it.id}|${it.title} (${it.date})" }
        )

        // Load participants
        val participants = dataStore.getAllParticipants()
        participantCombo.items = FXCollections.observableArrayList(
            participants.map { "${it.id}|${it.name} (${it.email})" }
        )

        if (events.isNotEmpty()) {
            eventCombo.selectionModel.selectFirst()
            loadRegisteredParticipants()
        }
    }

    private fun getSelectedEvent(): Event? {
        val selection = eventCombo.value ?: return null
        val eventId = selection.split("|").firstOrNull() ?: return null
        return dataStore.getEventById(eventId)
    }

    private fun loadRegisteredParticipants() {
        registeredList.clear()
        val event = getSelectedEvent() ?: return
        registeredList.addAll(dataStore.getParticipantsForEvent(event.id))
    }

    private fun registerParticipant() {
        val event = getSelectedEvent()
        if (event == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an event")
            return
        }

        val participantSelection = participantCombo.value
        if (participantSelection == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a participant")
            return
        }

        val participantId = participantSelection.split("|").firstOrNull()
        if (participantId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid participant selection")
            return
        }

        // Check capacity
        if (!event.hasAvailableCapacity()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Cannot register: Event has reached maximum capacity (${event.maxParticipants})")
            return
        }

        // Check if already registered
        if (event.isParticipantRegistered(participantId)) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Participant is already registered for this event")
            return
        }

        val success = dataStore.registerParticipantForEvent(participantId, event.id)
        if (success) {
            loadData()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Participant registered successfully!")
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register participant")
        }
    }

    private fun unregisterParticipant() {
        val event = getSelectedEvent()
        if (event == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an event")
            return
        }

        val selectedParticipant = registeredTable.selectionModel.selectedItem
        if (selectedParticipant == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a participant to unregister")
            return
        }

        val confirm = Alert(Alert.AlertType.CONFIRMATION,
            "Unregister '${selectedParticipant.name}' from this event?").showAndWait()

        if (confirm.isPresent && confirm.get() == ButtonType.OK) {
            val success = dataStore.unregisterParticipantFromEvent(selectedParticipant.id, event.id)
            if (success) {
                loadData()
                showAlert(Alert.AlertType.INFORMATION, "Success", "Participant unregistered successfully!")
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to unregister participant")
            }
        }
    }

    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }
}
