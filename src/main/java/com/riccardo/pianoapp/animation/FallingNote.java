package com.riccardo.pianoapp.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Represents a falling note in the animation.
 */
public class FallingNote extends Rectangle {
    private final int note;
    private long startTime;
    private Timeline animation;
    private double playbackSpeed;

    /**
     * Constructor for FallingNote.
     *
     * @param note       The MIDI note value.
     * @param startTime  The start time of the note.
     * @param x          The x-coordinate of the note.
     * @param y          The y-coordinate of the note.
     * @param width      The width of the note.
     * @param height     The height of the note.
     */
    public FallingNote(int note, long startTime, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.note = note;
        this.startTime = startTime;
        this.playbackSpeed = 1.0;

        this.setFill(Color.BLUEVIOLET);
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(1);

        this.setArcWidth(10);
        this.setArcHeight(10);
    }

    public int getNote() {
        return note;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setAnimation(Timeline animation) {
        this.animation = animation;
    }

    /**
     * Updates the speed of the falling note animation.
     *
     * @param playbackSpeed The new playback speed multiplier.
     */
    public void updateSpeed(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
        if (animation != null) {
            double currentTime = animation.getCurrentTime().toMillis();
            double remainingTime = (animation.getTotalDuration().toMillis() - currentTime) / playbackSpeed;
            animation.getKeyFrames().set(1, new KeyFrame(Duration.millis(remainingTime), new KeyValue(this.layoutYProperty(), this.getParent().getBoundsInLocal().getHeight())));
            animation.play();
        }
    }

    /**
     * Adjusts the start time of the falling note.
     *
     * @param elapsedTime The time elapsed since the animation was paused.
     */
    public void adjustStartTime(long elapsedTime) {
        this.startTime += elapsedTime;
    }

    /**
     * Pauses the falling note animation.
     */
    public void pauseAnimation() {
        if (animation != null) {
            animation.pause();
        }
    }

    /**
     * Resumes the falling note animation.
     */
    public void resumeAnimation() {
        if (animation != null) {
            animation.play();
        }
    }

    /**
     * Stops the falling note animation.
     */
    public void stopAnimation() {
        if (animation != null) {
            animation.stop();
        }
    }
}
