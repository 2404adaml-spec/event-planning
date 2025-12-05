package com.eventplanner.ui

import com.eventplanner.domain.Venue
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
 * View for managing venues.
 */
class VenueView(private val dataStore: DataStore) {
    val root: VBox = VBox(10.0)
    private val venueTable = TableView<Venue>()
    private val venueList = FXCollections.observableArrayList<Venue>()

    // Form fields
    private val nameField = TextField()
    private val capacityField = TextField()
    private val locationField = TextField()
    private val facilitiesField = TextField()
    private val hourlyRateField = TextField()

    init {
        setupUI()
        loadVenues()
    }

    private fun setupUI() {
        root.padding = Insets(15.0)

        // Form section
        val formGrid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(10.0)

            add(Label("Venue Name:"), 0, 0)
            add(nameField.apply { promptText = "Enter venue name" }, 1, 0)

            add(Label("Capacity:"), 0, 1)
            add(capacityField.apply { promptText = "Enter capacity" }, 1, 1)

            add(Label("Location:"), 0, 2)
            add(locationField.apply { promptText = "Enter location/address" }, 1, 2)

            add(Label("Facilities:"), 0, 3)
            add(facilitiesField.apply { promptText = "e.g., Projector, WiFi, AC" }, 1, 3)

            add(Label("Hourly Rate:"), 0, 4)
            add(hourlyRateField.apply { promptText = "Enter hourly rate" }, 1, 4)
        }

        // Buttons
        val buttonBox = HBox(10.0).apply {
            children.addAll(
                Button("Add Venue").apply {
                    setOnAction { addVenue() }
                    style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                },
                Button("Update Selected").apply {
                    setOnAction { updateVenue() }
                },
                Button("Delete Selected").apply {
                    setOnAction { deleteVenue() }
                    style = "-fx-background-color: #f44336; -fx-text-fill: white;"
                },
                Button("Clear Form").apply {
                    setOnAction { clearForm() }
                }
            )
        }

        // Table setup
        setupTable()

        // Selection listener to populate form
        venueTable.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            selected?.let { populateForm(it) }
        }

        VBox.setVgrow(venueTable, Priority.ALWAYS)

        root.children.addAll(
            Label("Venue Management").apply {
                style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            },
            formGrid,
            buttonBox,
            Separator(),
            venueTable
        )
    }

    private fun setupTable() {
        val idCol = TableColumn<Venue, String>("ID").apply {
            cellValueFactory = PropertyValueFactory("id")
            prefWidth = 100.0
        }

        val nameCol = TableColumn<Venue, String>("Name").apply {
            cellValueFactory = PropertyValueFactory("name")
            prefWidth = 150.0
        }

        val capacityCol = TableColumn<Venue, Int>("Capacity").apply {
            cellValueFactory = PropertyValueFactory("capacity")
            prefWidth = 80.0
        }

        val locationCol = TableColumn<Venue, String>("Location").apply {
            cellValueFactory = PropertyValueFactory("location")
            prefWidth = 200.0
        }

        val rateCol = TableColumn<Venue, Double>("Hourly Rate").apply {
            cellValueFactory = PropertyValueFactory("hourlyRate")
            prefWidth = 100.0
        }

        venueTable.columns.addAll(idCol, nameCol, capacityCol, locationCol, rateCol)
        venueTable.items = venueList
    }

    private fun loadVenues() {
        venueList.clear()
        venueList.addAll(dataStore.getAllVenues())
    }

    private fun addVenue() {
        try {
            val venue = Venue(
                name = nameField.text.trim(),
                capacity = capacityField.text.toIntOrNull() ?: throw IllegalArgumentException("Invalid capacity"),
                location = locationField.text.trim(),
                facilities = facilitiesField.text.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                hourlyRate = hourlyRateField.text.toDoubleOrNull() ?: 0.0
            )

            dataStore.addVenue(venue)
            loadVenues()
            clearForm()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Venue added successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add venue: ${e.message}")
        }
    }

    private fun updateVenue() {
        val selected = venueTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a venue to update")
            return
        }

        try {
            val updated = selected.copy(
                name = nameField.text.trim(),
                capacity = capacityField.text.toIntOrNull() ?: throw IllegalArgumentException("Invalid capacity"),
                location = locationField.text.trim(),
                facilities = facilitiesField.text.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                hourlyRate = hourlyRateField.text.toDoubleOrNull() ?: 0.0
            )

            dataStore.updateVenue(updated)
            loadVenues()
            showAlert(Alert.AlertType.INFORMATION, "Success", "Venue updated successfully!")
        } catch (e: Exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update venue: ${e.message}")
        }
    }

    private fun deleteVenue() {
        val selected = venueTable.selectionModel.selectedItem
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a venue to delete")
            return
        }

        val confirm = Alert(Alert.AlertType.CONFIRMATION, "Delete venue '${selected.name}'?").showAndWait()
        if (confirm.isPresent && confirm.get() == ButtonType.OK) {
            dataStore.deleteVenue(selected.id)
            loadVenues()
            clearForm()
        }
    }

    private fun populateForm(venue: Venue) {
        nameField.text = venue.name
        capacityField.text = venue.capacity.toString()
        locationField.text = venue.location
        facilitiesField.text = venue.facilities.joinToString(", ")
        hourlyRateField.text = venue.hourlyRate.toString()
    }

    private fun clearForm() {
        nameField.clear()
        capacityField.clear()
        locationField.clear()
        facilitiesField.clear()
        hourlyRateField.clear()
        venueTable.selectionModel.clearSelection()
    }

    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }
}
