package com.riccardo.pianoapp.sound;

import com.riccardo.pianoapp.PianoController;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configures key mappings for the piano application.
 */
public class KeyConfigurator {
    private final Map<Button, Integer> noteMap = new HashMap<>();
    private final Map<Integer, Button> reverseNoteMap = new HashMap<>();
    private final Map<Button, String> originalStyleMap = new HashMap<>();
    private final List<NoteListener> listeners = new ArrayList<>();

    private long pressTime;  // Variabile per memorizzare il tempo di pressione

    public void addListener(NoteListener listener) {
        listeners.add(listener);
    }

    private void notifyNoteOn(int note) {
        for (NoteListener listener : listeners) {
            listener.onNoteOn(note);
        }
    }

    private void notifyNoteOff(int note, long duration) {
        for (NoteListener listener : listeners) {
            listener.onNoteOff(note, duration);
        }
    }

    /**
     * Sets up the mappings between buttons and MIDI notes.
     *
     * @param noteMap          The map of buttons to MIDI notes.
     * @param reverseNoteMap   The map of MIDI notes to buttons.
     * @param originalStyleMap A map of original styles for buttons.
     * @param isAutoPlaying    Flag indicating if the application is in auto-playing mode.
     */
    public void setupNoteMaps(Map<Button, Integer> noteMap, Map<Integer, Button> reverseNoteMap, Map<Button, String> originalStyleMap, boolean isAutoPlaying) {
        this.noteMap.putAll(noteMap);
        this.reverseNoteMap.putAll(reverseNoteMap);
        this.originalStyleMap.putAll(originalStyleMap);

        for (Map.Entry<Button, Integer> entry : noteMap.entrySet()) {
            Button button = entry.getKey();
            int note = entry.getValue();
            reverseNoteMap.put(note, button);

            button.setOnMousePressed(event -> {
                if (!isAutoPlaying) {
                    playNoteWithAnimation(button, note);
                }
                pressTime = System.currentTimeMillis();  // Memorizza il tempo di pressione
            });

            button.setOnMouseReleased(event -> {
                if (!isAutoPlaying) {
                    long duration = System.currentTimeMillis() - pressTime;  // Calcola la durata della pressione
                    stopNoteWithAnimation(button, note, duration);
                }
            });
        }
    }

    private void playNoteWithAnimation(Button button, int note) {
        String originalStyle = button.getStyle();
        button.setStyle("-fx-background-color: #87CEEB;");
        PianoController.channel.noteOn(note, 900);
        button.setOnMouseReleased(event -> {
            button.setStyle(originalStyle);
            PianoController.channel.noteOff(note);
        });
    }

    private void stopNoteWithAnimation(Button button, int note, long duration) {
        if (button != null) {
            button.setStyle(originalStyleMap.get(button));
            button.getStyleClass().remove("pressed");
            notifyNoteOff(note, duration);  // Notifica l'evento di rilascio della nota con la durata
        }
    }
}
