package com.riccardo.pianoapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

/**
 * Main class for the Piano MIDI application.
 */
public class PianoApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("piano-view.fxml"));
        Parent root = loader.load();
        PianoController controller = loader.getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Piano MIDI");
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/riccardo/pianoapp/icons/pianoapp.png")));
        primaryStage.getIcons().add(icon);

        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
