package com.riccardo.pianoapp.animation;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javax.sound.midi.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.riccardo.pianoapp.PianoController.channel;

/**
 * Manages the animations of falling notes and the progress bar.
 */
public class AnimationManager implements PlaybackObserver {
    private final Pane notePane;
    private final ProgressBar progressBar;
    private final Map<Button, String> originalStyleMap;
    private double playbackSpeed;

    /**
     * Constructor for AnimationManager.
     *
     * @param notePane         The pane in which notes are animated.
     * @param progressBar      The progress bar to indicate playback progress.
     * @param originalStyleMap The map to keep track of the original key style for buttons.
     */
    public AnimationManager(Pane notePane, ProgressBar progressBar, Map<Button, String> originalStyleMap) {
        this.notePane = notePane;
        this.progressBar = progressBar;
        this.originalStyleMap = originalStyleMap;
        this.playbackSpeed = 1.0;  // Default playback speed
    }

    /**
     * Starts the note animations based on the provided MIDI sequence.
     *
     * @param sequence       The MIDI sequence to be played.
     * @param startTick      The tick to start playback from.
     * @param bpm            The playback speed in beats per minute.
     * @param playbackSpeed  The playback speed changer.
     * @param notesPlayed    The atomic integer tracking notes played.
     * @param totalNotes     The total number of notes in the MIDI sequence.
     * @param reverseNoteMap The map of note values to buttons.
     */
    public void startNoteAnimations(Sequence sequence, long startTick, int bpm, double playbackSpeed, AtomicInteger notesPlayed, int totalNotes, Map<Integer, Button> reverseNoteMap) {
        long startTime = System.currentTimeMillis();

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getMessage() instanceof ShortMessage sm) {
                    int note = sm.getData1();
                    boolean noteOn = sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0;
                    if (event.getTick() >= startTick) {
                        long eventTimestamp = (long) (startTime + ((event.getTick() - startTick) * (1000.0 / (sequence.getResolution() * (bpm / 60.0)))));

                        if (noteOn) {
                            Button keyButton = reverseNoteMap.get(note);
                            if (keyButton != null) {
                                long noteDuration = getNoteDuration(track, i);
                                Platform.runLater(() -> scheduleFallingNoteAnimation(note, eventTimestamp, keyButton, noteDuration, totalNotes, notesPlayed, playbackSpeed, notePane, channel));
                            }
                        }
                    }
                }
            }
        }
    }

    private long getNoteDuration(Track track, int noteIndex) {
        for (int i = noteIndex + 1; i < track.size(); i++) {
            MidiEvent nextEvent = track.get(i);
            if (nextEvent.getMessage() instanceof ShortMessage sm) {
                if (sm.getCommand() == ShortMessage.NOTE_OFF || (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                    return nextEvent.getTick() - track.get(noteIndex).getTick();
                }
            }
        }
        return 0;
    }

    /**
     * Schedules the animation of a falling musical note and handles the note consumption when the animation finishes.
     *
     * @param note              The MIDI note value of the falling note.
     * @param eventTimestamp    The timestamp at which the note event occurs.
     * @param keyButton         The button representing the key associated with the note.
     * @param noteDuration      The duration of the note in milliseconds.
     * @param totalNotes        The total number of notes to be played.
     * @param notesPlayed       The atomic integer tracking the number of notes played.
     * @param playbackSpeed     The speed at which the note should fall.
     * @param notePane          The pane in which the falling note animation appears.
     * @param channel           The MIDI channel used for playback.
     */
    private void scheduleFallingNoteAnimation(int note, long eventTimestamp, Button keyButton, long noteDuration, int totalNotes, AtomicInteger notesPlayed, double playbackSpeed, Pane notePane, MidiChannel channel) {
        if (keyButton == null) return;

        double x = keyButton.getLayoutX();
        double noteHeight = 20 + (noteDuration / 10.0);
        double paneHeight = notePane.getHeight();
        double startY = -noteHeight;

        FallingNote fallingNote = new FallingNote(note, eventTimestamp, x, startY, keyButton.getWidth(), noteHeight);
        notePane.getChildren().add(fallingNote);

        double distance = paneHeight - noteHeight;
        double animationDurationMillis = distance / playbackSpeed;

        long delay = Math.max(0, eventTimestamp - System.currentTimeMillis());

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(fallingNote.layoutYProperty(), startY)),
                new KeyFrame(Duration.millis(animationDurationMillis), new KeyValue(fallingNote.layoutYProperty(), paneHeight - noteHeight, Interpolator.LINEAR))
        );
        timeline.setDelay(Duration.millis(delay));
        timeline.setOnFinished(event -> {
            notePane.getChildren().remove(fallingNote);
            NoteConsumer noteConsumer = new NoteConsumer(keyButton, note, noteDuration, x, paneHeight - noteHeight, keyButton.getWidth(), noteHeight, originalStyleMap, notePane);
            notePane.getChildren().add(noteConsumer);
            noteConsumer.startConsumption(notePane);
            noteConsumer.playNote();

            int played = notesPlayed.incrementAndGet();
            Platform.runLater(() -> {
                progressBar.setProgress((double) played / totalNotes);
                if (played == totalNotes) {
                    progressBar.setProgress(1.0);
                }
            });
        });
        timeline.play();
        fallingNote.setAnimation(timeline);
    }

    /**
     * Updates the animation speeds for all falling notes.
     *
     * @param playbackSpeed     The new playback speed multiplier.
     */
    public void updateAnimationSpeeds(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
        notePane.getChildren().forEach(node -> {
            if (node instanceof FallingNote) {
                ((FallingNote) node).updateSpeed(playbackSpeed);
            }
        });
    }

    /**
     * Pauses all animations.
     */
    public void pauseAnimations() {
        notePane.getChildren().forEach(node -> {
            if (node instanceof FallingNote) {
                ((FallingNote) node).pauseAnimation();
            }
        });
    }

    /**
     * Resumes all animations.
     *
     * @param elapsedTime   The time elapsed since the animations were paused.
     */
    public void resumeAnimations(long elapsedTime) {
        notePane.getChildren().forEach(node -> {
            if (node instanceof FallingNote) {
                ((FallingNote) node).adjustStartTime(elapsedTime);
                ((FallingNote) node).resumeAnimation();
            }
        });
    }

    /**
     * Stops all animations and clears the note pane.
     */
    public void stopAnimations() {
        notePane.getChildren().forEach(node -> {
            if (node instanceof FallingNote) {
                ((FallingNote) node).stopAnimation();
            }
        });
        notePane.getChildren().clear();
    }

    /**
     * Updates playback speed based on playback settings or user preferences.
     */
    @Override
    public void onPlaybackStarted() {
        // Handle playback start event if necessary.
    }

    @Override
    public void onPlaybackPaused() {
        pauseAnimations();
    }

    @Override
    public void onPlaybackStopped() {
        stopAnimations();
    }
}
