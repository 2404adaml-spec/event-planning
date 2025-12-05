package com.eventplanner

import com.eventplanner.ui.MainView
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Main entry point for the Event Planning Application.
 */
class EventPlannerApp : Application() {

    override fun start(primaryStage: Stage) {
        val mainView = MainView()

        val scene = Scene(mainView.root, 1200.0, 800.0)

        // Apply CSS styling
        scene.stylesheets.add(javaClass.getResource("/styles.css")?.toExternalForm() ?: "")

        primaryStage.apply {
            title = "Event Planning Application"
            this.scene = scene
            minWidth = 900.0
            minHeight = 600.0
        }

        primaryStage.show()
    }
}

fun main() {
    Application.launch(EventPlannerApp::class.java)
}
