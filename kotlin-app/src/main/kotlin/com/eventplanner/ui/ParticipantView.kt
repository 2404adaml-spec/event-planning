package com.eventplanner.ui

import com.eventplanner.domain.Participant
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
 * View for managing participants.
 */
class ParticipantView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)
    private val participantTable = TableView<Participant>()
    private val participantList = FXCollections.observableArrayList<Participant>()

    // Form fields
    private val nameField = TextField()
    private val emailField = TextField()
    private val phoneField = TextField()
    private val organizationField = TextField()

    init {
        setupUI()
        loadParticipants()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Form section
        val formGrid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(10.0)

            add(Label("Name:"), 0, 0)
            add(nameField.apply { promptText = "Enter participant name" }, 1, 0)

            add(Label("Email:"), 0, 1)
            add(emailField.apply { promptText = "Enter email address" }, 1, 1)

            add(Label("Phone:"), 0, 2)
            add(phoneField.apply { promptText = "Enter phone number" }, 1, 2)

            add(Label("Organization:"), 0, 3)
            add(organizationField.apply { promptText = "Enter organization" }, 1, 3)
        }

        // Buttons
        val buttonBox = HBox(10.0).apply {
            children.addAll(
                Button("Add Participant").apply {
                    setOnAction { addParticipant() }
                    style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                },
                Button("Update Selected").apply {
                    setOnAction { updateParticipant() }
                },
                Button("Delete Selected").apply {
                    setOnAction { deleteParticipant() }
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
        participantTable.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            selected?.let { populateForm(it) }
        }

        VBox.setVgrow(participantTable, Priority.ALWAYS)

        root.children.addAll(
            Label("Participant Management").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            formGrid,
            buttonBox,
            Separator(),
            participantTable
        )
    }

    private fun setupTable() {
        val idCol = TableColumn<Participant, String>("ID").apply {
            cellValueFactory = PropertyValueFactory("id")
            prefWidth = 100.0
        }

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

        participantTable.columns.addAll(idCol, nameCol, emailCol, phoneCol, orgCol)
        participantTable.items = participantList
    }

    private fun loadParticipants() {
        participantList.clear()
        participantList.addAll(dataStore.getAllParticipants())
    }

    private fun addParticipant() {
        try {
            val participant = Participant(
                name = nameField.text.trim(),
                email = emailField.text.trim(),
                phone = phoneField.text.trim(),
                organization = organizationField.text.trim()
            )

            dataStore.addParticipant(participant)
            loadParticipants()
            clearForm()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Participant added successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add participant: ${e.message}")
        }
    }

    private fun updateParticipant() {
        val selected = participantTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a participant to update")
            return
        }

        try {
            val updated = selected.copy(
                name = nameField.text.trim(),
                email = emailField.text.trim(),
                phone = phoneField.text.trim(),
                organization = organizationField.text.trim()
            )

            dataStore.updateParticipant(updated)
            loadParticipants()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Participant updated successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update participant: ${e.message}")
        }
    }

    private fun deleteParticipant() {
        val selected = participantTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a participant to delete")
            return
        }

        val confirm = Alert(Alert.AlertType.CONFIRMATION, "Delete participant '${selected.name}'?").showAndWait()
        if (confirm.isPresent && confirm.get() == ButtonType.OK) {
            dataStore.deleteParticipant(selected.id)
            loadParticipants()
            clearForm()
        }
    }

    private fun populateForm(participant: Participant) {
        nameField.text = participant.name
        emailField.text = participant.email
        phoneField.text = participant.phone
        organizationField.text = participant.organization
    }

    private fun clearForm() {
        nameField.clear()
        emailField.clear()
        phoneField.clear()
        organizationField.clear()
        participantTable.selectionModel.clearSelection()
    }

    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }
}
