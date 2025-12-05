package com.eventplanner.ui

import com.eventplanner.domain.Session
import com.eventplanner.persistence.DataStore
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * View for managing sessions within events.
 */
class SessionView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)

    private val eventCombo = ComboBox<String>()
    private val sessionTable = TableView<Session>()
    private val sessionList = FXCollections.observableArrayList<Session>()

    // Form fields
    private val titleField = TextField()
    private val speakerField = TextField()
    private val descriptionArea = TextArea()
    private val startTimeField = TextField()
    private val endTimeField = TextField()
    private val roomField = TextField()

    init {
        setupUI()
        loadEvents()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Event selection
        val eventBox = HBox(10.0).apply {
            children.addAll(
                Label("Select Event:"),
                eventCombo.apply {
                    prefWidth = 300.0
                    setOnAction { loadSessions() }
                },
                Button("Refresh").apply {
                    setOnAction { loadEvents() }
                }
            )
        }

        // Form section
        val formGrid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(10.0)

            add(Label("Session Title:"), 0, 0)
            add(titleField.apply { promptText = "Enter session title" }, 1, 0)

            add(Label("Speaker:"), 0, 1)
            add(speakerField.apply { promptText = "Enter speaker name" }, 1, 1)

            add(Label("Description:"), 0, 2)
            add(descriptionArea.apply {
                promptText = "Enter session description"
                prefRowCount = 2
            }, 1, 2)

            add(Label("Start Time (HH:MM):"), 0, 3)
            add(startTimeField.apply { promptText = "e.g., 09:00" }, 1, 3)

            add(Label("End Time (HH:MM):"), 0, 4)
            add(endTimeField.apply { promptText = "e.g., 10:30" }, 1, 4)

            add(Label("Room:"), 0, 5)
            add(roomField.apply { promptText = "e.g., Room A" }, 1, 5)
        }

        // Buttons
        val buttonBox = HBox(10.0).apply {
            children.addAll(
                Button("Add Session").apply {
                    setOnAction { addSession() }
                    style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                },
                Button("Update Selected").apply {
                    setOnAction { updateSession() }
                },
                Button("Delete Selected").apply {
                    setOnAction { deleteSession() }
                    style = "-fx-background-color: #f44336; -fx-text-fill: white;"
                },
                Button("Clear Form").apply {
                    setOnAction { clearForm() }
                }
            )
        }

        // Table setup
        setupTable()

        // Selection listener
        sessionTable.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            selected?.let { populateForm(it) }
        }

        VBox.setVgrow(sessionTable, Priority.ALWAYS)

        root.children.addAll(
            Label("Session Management").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            Label("Manage sessions within multi-session events like conferences").apply {
                style = "-fx-font-style: italic;"
            },
            eventBox,
            Separator(),
            formGrid,
            buttonBox,
            Separator(),
            Label("Sessions for Selected Event:").apply { style = "-fx-font-weight: bold;" },
            sessionTable
        )
    }

    private fun setupTable() {
        val titleCol = TableColumn<Session, String>("Title").apply {
            cellValueFactory = PropertyValueFactory("title")
            prefWidth = 150.0
        }

        val speakerCol = TableColumn<Session, String>("Speaker").apply {
            cellValueFactory = PropertyValueFactory("speaker")
            prefWidth = 120.0
        }

        val timeCol = TableColumn<Session, String>("Time").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(
                    "${cellData.value.startTime}-${cellData.value.endTime}"
                )
            }
            prefWidth = 100.0
        }

        val roomCol = TableColumn<Session, String>("Room").apply {
            cellValueFactory = PropertyValueFactory("room")
            prefWidth = 80.0
        }

        val durationCol = TableColumn<Session, String>("Duration").apply {
            setCellValueFactory { cellData ->
                javafx.beans.property.SimpleStringProperty(
                    "${cellData.value.durationMinutes()} min"
                )
            }
            prefWidth = 80.0
        }

        sessionTable.columns.addAll(titleCol, speakerCol, timeCol, roomCol, durationCol)
        sessionTable.items = sessionList
    }

    private fun loadEvents() {
        val events = dataStore.getAllEvents()
        eventCombo.items = FXCollections.observableArrayList(
            events.map { "${it.id}|${it.title} (${it.date})" }
        )
        if (events.isNotEmpty()) {
            eventCombo.selectionModel.selectFirst()
            loadSessions()
        }
    }

    private fun getSelectedEventId(): String? {
        return eventCombo.value?.split("|")?.firstOrNull()
    }

    private fun loadSessions() {
        sessionList.clear()
        val eventId = getSelectedEventId() ?: return
        sessionList.addAll(dataStore.getSessionsByEventId(eventId))
    }

    private fun addSession() {
        val eventId = getSelectedEventId()
        if (eventId == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an event first")
            return
        }

        try {
            validateTimeFormat(startTimeField.text.trim(), "Start time")
            validateTimeFormat(endTimeField.text.trim(), "End time")

            val session = Session(
                eventId = eventId,
                title = titleField.text.trim(),
                speaker = speakerField.text.trim(),
                description = descriptionArea.text.trim(),
                startTime = startTimeField.text.trim(),
                endTime = endTimeField.text.trim(),
                room = roomField.text.trim()
            )

            dataStore.addSession(session)
            loadSessions()
            clearForm()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Session added successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add session: ${e.message}")
        }
    }

    private fun updateSession() {
        val selected = sessionTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a session to update")
            return
        }

        try {
            validateTimeFormat(startTimeField.text.trim(), "Start time")
            validateTimeFormat(endTimeField.text.trim(), "End time")

            val updated = selected.copy(
                title = titleField.text.trim(),
                speaker = speakerField.text.trim(),
                description = descriptionArea.text.trim(),
                startTime = startTimeField.text.trim(),
                endTime = endTimeField.text.trim(),
                room = roomField.text.trim()
            )

            dataStore.updateSession(updated)
            loadSessions()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Session updated successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update session: ${e.message}")
        }
    }

    private fun deleteSession() {
        val selected = sessionTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a session to delete")
            return
        }

        val confirm = Alert(Alert.AlertType.CONFIRMATION, "Delete session '${selected.title}'?").showAndWait()
        if (confirm.isPresent && confirm.get() == ButtonType.OK) {
            dataStore.deleteSession(selected.id)
            loadSessions()
            clearForm()
        }
    }

    private fun populateForm(session: Session) {
        titleField.text = session.title
        speakerField.text = session.speaker
        descriptionArea.text = session.description
        startTimeField.text = session.startTime
        endTimeField.text = session.endTime
        roomField.text = session.room
    }

    private fun clearForm() {
        titleField.clear()
        speakerField.clear()
        descriptionArea.clear()
        startTimeField.clear()
        endTimeField.clear()
        roomField.clear()
        sessionTable.selectionModel.clearSelection()
    }

    private fun validateTimeFormat(time: String, fieldName: String) {
        if (time.isEmpty()) {
            throw IllegalArgumentException("$fieldName is required")
        }
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        if (!regex.matches(time)) {
            throw IllegalArgumentException("$fieldName must be in HH:MM format (e.g., 09:00)")
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
