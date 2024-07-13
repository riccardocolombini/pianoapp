package com.riccardo.pianoapp.sound;

import com.riccardo.pianoapp.recording.RecordingManager;
import javafx.scene.control.Button;
import javax.sound.midi.MidiChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles playing and stopping notes with animation and recording.
 */
public class NoteHandler {
    private final MidiChannel channel;
    private final Map<Button, String> originalStyleMap;
    private final RecordingManager recordingManager;
    private final Map<Button, Long> noteStartTimes = new ConcurrentHashMap<>();
    private final Map<Button, Integer> noteMap;

    /**
     * Constructor for NoteHandler.
     *
     * @param channel           The MIDI channel to be used.
     * @param originalStyleMap  A map of original styles for buttons.
     * @param recordingManager  The recording manager.
     * @param noteMap           The map of buttons to MIDI notes.
     */
    public NoteHandler(MidiChannel channel, Map<Button, String> originalStyleMap, RecordingManager recordingManager, Map<Button, Integer> noteMap) {
        this.channel = channel;
        this.originalStyleMap = originalStyleMap;
        this.recordingManager = recordingManager;
        this.noteMap = noteMap;
    }

    /**
     * Plays a note with animation and starts recording.
     *
     * @param button The button associated with the note.
     * @param note   The MIDI note value.
     */
    public void playNoteWithAnimation(Button button, int note) {
        String originalStyle = button.getStyle();
        button.setStyle("-fx-background-color: #87CEEB;");
        channel.noteOn(note, 900);

        long startTime = System.currentTimeMillis();
        noteStartTimes.put(button, startTime);

        button.setOnMouseReleased(event -> {
            button.setStyle(originalStyle);
            channel.noteOff(note);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            recordingManager.recordNoteOff(note, duration);
        });

        recordingManager.recordNoteOn(note);
    }

    /**
     * Stops a note with animation and stops recording.
     *
     * @param button The button associated with the note.
     */
    public void stopNoteWithAnimation(Button button) {
        if (button != null) {
            button.setStyle(originalStyleMap.get(button));
            button.getStyleClass().remove("pressed");
            long startTime = noteStartTimes.getOrDefault(button, System.currentTimeMillis());
            long duration = System.currentTimeMillis() - startTime;
            int note = noteMap.get(button);
            recordingManager.recordNoteOff(note, duration);
        }
    }
}
