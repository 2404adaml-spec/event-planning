package com.eventplanner.ui

import com.eventplanner.persistence.DataStore
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

/**
 * Main view of the application containing all tabs.
 */
class MainView {
    val root: BorderPane = BorderPane()
    private val dataStore = DataStore()

    init {
        setupUI()
    }

    private fun setupUI() {
        // Create header
        val header = Label("Event Planning Application").apply {
            style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 15px;"
        }

        // Create tab pane
        val tabPane = TabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            // Venues tab (must be first to create venues for events)
            tabs.add(Tab("Venues").apply {
                content = VenueView(dataStore).root
            })

            // Events tab
            tabs.add(Tab("Events").apply {
                content = EventView(dataStore).root
            })

            // Sessions tab (for multi-session events)
            tabs.add(Tab("Sessions").apply {
                content = SessionView(dataStore).root
            })

            // Participants tab
            tabs.add(Tab("Participants").apply {
                content = ParticipantView(dataStore).root
            })

            // Registration tab
            tabs.add(Tab("Registration").apply {
                content = RegistrationView(dataStore).root
            })

            // Slot Finder tab
            tabs.add(Tab("Slot Finder").apply {
                content = SlotFinderView(dataStore).root
            })

            // Scheduler tab
            tabs.add(Tab("Event Scheduler").apply {
                content = SchedulerView(dataStore).root
            })
        }

        // Create status bar
        val statusBar = Label("Ready").apply {
            style = "-fx-padding: 5px; -fx-background-color: #f0f0f0;"
            maxWidth = Double.MAX_VALUE
        }

        root.apply {
            top = VBox(header)
            center = tabPane
            bottom = statusBar
            padding = Insets(10.0)
        }
    }
}
