package com.riccardo.pianoapp.animation;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Map;

import static com.riccardo.pianoapp.PianoController.channel;


/**
 * Consumes a note by animating it and updating the corresponding key button.
 */
public class NoteConsumer extends Rectangle {
    private final Button keyButton;
    private final int note;
    private final long noteDuration;
    private final Map<Button, String> originalStyleMap;
    private Timeline consumptionTimeline;

    /**
     * Constructor for NoteConsumer.
     *
     * @param keyButton        The key button associated with the note.
     * @param note             The MIDI note value.
     * @param noteDuration     The duration of the note.
     * @param x                The x-coordinate of the note.
     * @param y                The y-coordinate of the note.
     * @param width            The width of the note.
     * @param height           The height of the note.
     * @param originalStyleMap A map of original styles for buttons.
     * @param notePane         The pane where the note is animated.
     */
    public NoteConsumer(Button keyButton, int note, long noteDuration, double x, double y, double width, double height, Map<Button, String> originalStyleMap, Pane notePane) {
        super(x, y, width, height);
        this.keyButton = keyButton;
        this.note = note;
        this.noteDuration = noteDuration;
        this.originalStyleMap = originalStyleMap;

        this.setFill(Color.LIGHTBLUE);
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(1);

        this.setArcWidth(10);
        this.setArcHeight(10);

        this.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() <= 0) {
                notePane.getChildren().remove(this);
            }
        });
    }

    /**
     * Starts the consumption animation for the note.
     *
     * @param pane The pane where the note is animated.
     */
    public void startConsumption(Pane pane) {
        keyButton.setStyle("-fx-background-color: #87CEEB;");
        consumptionTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(this.yProperty(), this.getY()), new KeyValue(this.heightProperty(), this.getHeight())),
                new KeyFrame(Duration.millis(noteDuration), new KeyValue(this.yProperty(), this.getY() + this.getHeight()), new KeyValue(this.heightProperty(), 0, Interpolator.LINEAR))
        );
        consumptionTimeline.setOnFinished(event -> {
            keyButton.setStyle(originalStyleMap.get(keyButton));
            pane.getChildren().remove(this);
        });
        consumptionTimeline.play();
    }

    /**
     * Plays the note associated with this consumer.
     */
    public void playNote() {
        channel.noteOn(note, 900);
        Timeline noteOffTimeline = new Timeline(new KeyFrame(Duration.millis(noteDuration), ae -> {
            channel.noteOff(note);
            keyButton.setStyle(originalStyleMap.get(keyButton));
        }));
        noteOffTimeline.play();
    }
}
