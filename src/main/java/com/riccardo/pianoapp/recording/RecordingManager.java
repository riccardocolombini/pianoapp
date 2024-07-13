package com.riccardo.pianoapp.recording;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages recording of MIDI events.
 */
public class RecordingManager {
    private boolean isRecording = false;
    private Sequence recordingSequence;
    private Track recordingTrack;
    private long recordingStartTime;
    private final Map<Integer, Long> noteStartTimes = new ConcurrentHashMap<>();
    private final Label recordingTimeLabel;
    private final Button recordButton;
    private final Timeline recordingTimeline = new Timeline();

    /**
     * Constructor for RecordingManager.
     *
     * @param recordingTimeLabel The label to display recording time.
     * @param recordButton       The button to start/stop recording.
     */
    public RecordingManager(Label recordingTimeLabel, Button recordButton) {
        this.recordingTimeLabel = recordingTimeLabel;
        this.recordButton = recordButton;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getRecordingStartTime() {
        return recordingStartTime;
    }

    /**
     * Starts recording MIDI events.
     */
    public void startRecording() {
        try {
            recordingSequence = new Sequence(Sequence.PPQ, 24);
            recordingTrack = recordingSequence.createTrack();
            recordingStartTime = System.currentTimeMillis();

            MetaMessage metaMessage = new MetaMessage();
            metaMessage.setMessage(0x03, "Piano Recording".getBytes(), "Piano Recording".length());
            MidiEvent metaEvent = new MidiEvent(metaMessage, 0);
            recordingTrack.add(metaEvent);

            isRecording = true;
            recordButton.setText("Stop REC");
            startRecordingTimer();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops recording MIDI events and saves the recording to a file.
     */
    public void stopRecording() {
        isRecording = false;
        recordButton.setText("REC");
        recordingTimeline.stop();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MIDI files (*.mid, *.midi)", "*.mid", "*.midi"));
        fileChooser.setInitialFileName("recording.mid");
        File saveFile = fileChooser.showSaveDialog(null);

        if (saveFile != null) {
            try {
                MidiSystem.write(recordingSequence, 1, saveFile);
                showInformation();
            } catch (IOException e) {
                e.printStackTrace();
                showError();
            }
        }

        recordingTimeLabel.setText("00:00");
    }

    /**
     * Records a note-on event.
     *
     * @param note The MIDI note value.
     */
    public void recordNoteOn(int note) {
        if (recordingTrack == null) {
            return;
        }

        long tick = (System.currentTimeMillis() - recordingStartTime) / 10;
        noteStartTimes.put(note, tick);

        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_ON, 0, note, 64);
            MidiEvent event = new MidiEvent(message, tick);
            recordingTrack.add(event);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    /**
     * Records a note-off event.
     *
     * @param note     The MIDI note value.
     * @param duration The duration of the note.
     */
    public void recordNoteOff(int note, long duration) {
        if (recordingTrack == null) {
            return;
        }

        long startTick = noteStartTimes.get(note);
        long endTick = startTick + (duration / 10);

        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, 0, note, 64);
            MidiEvent event = new MidiEvent(message, endTick);
            recordingTrack.add(event);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    private void startRecordingTimer() {
        recordingTimeline.setCycleCount(Timeline.INDEFINITE);
        recordingTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1), event -> updateRecordingTime())
        );
        recordingTimeline.playFromStart();
    }

    private void updateRecordingTime() {
        long elapsedMillis = System.currentTimeMillis() - recordingStartTime;
        long elapsedSeconds = elapsedMillis / 1000;
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;
        Platform.runLater(() -> recordingTimeLabel.setText(String.format("%02d:%02d", minutes, seconds)));
    }

    private void showInformation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Recording saved successfully!");
        alert.showAndWait();
    }

    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Failed to save recording.");
        alert.showAndWait();
    }
}
