package com.riccardo.pianoapp;

import javafx.scene.control.Alert;

/**
 * Manages UI interactions and feedback.
 */
public class UiManager {

    public UiManager() {
    }

    /**
     * Shows an error alert with the provided message.
     *
     * @param message The error message to be displayed.
     */
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
