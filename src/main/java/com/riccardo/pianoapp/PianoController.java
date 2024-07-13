package com.riccardo.pianoapp;

import com.riccardo.pianoapp.animation.AnimationManager;
import com.riccardo.pianoapp.midi.MidiManager;
import com.riccardo.pianoapp.recording.RecordingManager;
import com.riccardo.pianoapp.sound.NoteHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for the Piano MIDI application.
 */
public class PianoController implements Initializable {
    public static MidiChannel channel;
    private Sequence sequence;
    private Synthesizer synthesizer;
    private double playbackSpeed = 1.0;
    private boolean isAutoPlaying = false;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean isSequenceLoaded = false;
    private long pauseTime;
    private int bpm = 120;
    private long currentTick = 0;

    @FXML
    private Button cKey, cDKey, dKey, dDKey, eKey, fKey, fDKey, gKey, gDKey, aKey, aDKey, bKey;
    @FXML
    private Button cKey1, dKey1, eKey1, fKey1, gKey1, aKey1, bKey1;
    @FXML
    private Button cDKey1, dDKey1, fDKey1, gDKey1, aDKey1;
    @FXML
    private Button cKey2, cDKey2, dKey2, dDKey2, eKey2, fKey2, fDKey2, gKey2, gDKey2, aKey2, aDKey2, bKey2;
    @FXML
    private Button loadMidiButton, playButton, pauseButton, stopButton;
    @FXML
    private Pane notePane;
    @FXML
    private TextField bpmTextField;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Button recordButton;
    @FXML
    private Label recordingTimeLabel;

    private final Map<Button, Integer> noteMap = new HashMap<>();
    private final Map<Integer, Button> reverseNoteMap = new HashMap<>();
    private final Map<Button, String> originalStyleMap = new HashMap<>();

    private AnimationManager animationManager;
    private RecordingManager recordingManager;
    private MidiManager midiManager;
    private UiManager uiManager;
    private NoteHandler noteHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeSynthesizer();
            setupManagers();
            setupNoteMaps();
            saveOriginalStyles();
            setupButtonActions();
            fileNameLabel.setText("No file loaded");

        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the primary stage for the application.
     *
     * @param stage The primary stage.
     */
    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> closeResources());
    }

    private void initializeSynthesizer() throws MidiUnavailableException, InvalidMidiDataException, IOException {
        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        String soundFontPath = "src/main/resources/com/riccardo/pianoapp/sound/yamaha_grand_piano.sf2";
        Soundbank soundbank = MidiSystem.getSoundbank(new File(soundFontPath));
        synthesizer.loadAllInstruments(soundbank);
        channel = synthesizer.getChannels()[0];
        channel.programChange(0);
    }

    private void setupManagers() {
        animationManager = new AnimationManager(notePane, progressBar, originalStyleMap);
        recordingManager = new RecordingManager(recordingTimeLabel, recordButton);
        midiManager = new MidiManager(synthesizer, channel);
        uiManager = new UiManager();
        noteHandler = new NoteHandler(channel, originalStyleMap, recordingManager, noteMap);
    }

    private void setupNoteMaps() {
        noteMap.put(cKey, 36);
        noteMap.put(cDKey, 37);
        noteMap.put(dKey, 38);
        noteMap.put(dDKey, 39);
        noteMap.put(eKey, 40);
        noteMap.put(fKey, 41);
        noteMap.put(fDKey, 42);
        noteMap.put(gKey, 43);
        noteMap.put(gDKey, 44);
        noteMap.put(aKey, 45);
        noteMap.put(aDKey, 46);
        noteMap.put(bKey, 47);
        noteMap.put(cKey1, 48);
        noteMap.put(dKey1, 50);
        noteMap.put(eKey1, 52);
        noteMap.put(fKey1, 53);
        noteMap.put(gKey1, 55);
        noteMap.put(aKey1, 57);
        noteMap.put(bKey1, 59);
        noteMap.put(cDKey1, 49);
        noteMap.put(dDKey1, 51);
        noteMap.put(fDKey1, 54);
        noteMap.put(gDKey1, 56);
        noteMap.put(aDKey1, 58);
        noteMap.put(cKey2, 60);
        noteMap.put(dKey2, 62);
        noteMap.put(eKey2, 64);
        noteMap.put(fKey2, 65);
        noteMap.put(gKey2, 67);
        noteMap.put(aKey2, 69);
        noteMap.put(bKey2, 71);
        noteMap.put(cDKey2, 61);
        noteMap.put(dDKey2, 63);
        noteMap.put(fDKey2, 66);
        noteMap.put(gDKey2, 68);
        noteMap.put(aDKey2, 70);

        for (Map.Entry<Button, Integer> entry : noteMap.entrySet()) {
            Button button = entry.getKey();
            int note = entry.getValue();
            reverseNoteMap.put(note, button);

            button.setOnMousePressed(event -> {
                noteHandler.playNoteWithAnimation(button, note);
                if (recordingManager.isRecording()) {
                    recordingManager.recordNoteOn(note);
                }
            });
            button.setOnMouseReleased(event -> {
                noteHandler.stopNoteWithAnimation(button);
                if (recordingManager.isRecording()) {
                    recordingManager.recordNoteOff(note, System.currentTimeMillis() - recordingManager.getRecordingStartTime());
                }
            });
        }
    }

    private void saveOriginalStyles() {
        noteMap.keySet().forEach(button -> originalStyleMap.put(button, button.getStyle()));
    }

    private void setupButtonActions() {
        loadMidiButton.setOnAction(event -> loadMidiFile());
        playButton.setOnAction(event -> playMidi());
        stopButton.setOnAction(event -> stopMidi());
        pauseButton.setOnAction(event -> pauseMidi());
        recordButton.setOnAction(event -> toggleRecording());
        bpmTextField.setOnAction(event -> updateBpm());
    }

    private void loadMidiFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MIDI files (*.mid, *.midi)", "*.mid", "*.midi"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                sequence = MidiSystem.getSequence(selectedFile);
                isAutoPlaying = false;
                isSequenceLoaded = true;
                fileNameLabel.setText(selectedFile.getName());

                int defaultBpm = midiManager.getBpmFromMidi(sequence);
                bpmTextField.setText(String.valueOf(defaultBpm));
                bpm = defaultBpm;
                playbackSpeed = bpm / 120.0;
                updateAnimationSpeeds();

            } catch (InvalidMidiDataException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playMidi() {
        if (!isPlaying) {
            if (sequence != null && isSequenceLoaded) {
                if (isPaused) {
                    resumeAnimations();
                } else {
                    isAutoPlaying = true;
                    startNoteAnimations(currentTick);
                }
                isPlaying = true;
                isPaused = false;
            } else {
                System.out.println("No MIDI sequence loaded.");
            }
        } else {
            System.out.println("MIDI is already playing.");
        }
    }

    private void stopMidi() {
        stopAnimations();
        isPlaying = false;
        isPaused = false;
        isAutoPlaying = false;
        currentTick = 0;
    }

    private void pauseMidi() {
        if (isPlaying) {
            pauseAnimations();
            isPlaying = false;
            isPaused = true;
        }
    }

    private void toggleRecording() {
        if (recordingManager.isRecording()) {
            recordingManager.stopRecording();
        } else {
            recordingManager.startRecording();
        }
    }

    private void updateBpm() {
        try {
            int newBpm = Integer.parseInt(bpmTextField.getText());
            if (newBpm >= 40 && newBpm <= 240) {
                bpm = newBpm;
                playbackSpeed = bpm / 120.0;
                updateAnimationSpeeds();
            } else {
                uiManager.showError("BPM must be between 40 and 240.");
            }
        } catch (NumberFormatException e) {
            uiManager.showError("Invalid BPM value.");
        }
    }

    private void startNoteAnimations(long startTick) {
        if (sequence == null) {
            System.out.println("No MIDI sequence loaded.");
            return;
        }

        int totalNotes = midiManager.countTotalNotes(sequence);
        AtomicInteger notesPlayed = new AtomicInteger(0);
        progressBar.setProgress(0);

        animationManager.startNoteAnimations(sequence, startTick, bpm, playbackSpeed, notesPlayed, totalNotes, reverseNoteMap);
    }

    private void updateAnimationSpeeds() {
        animationManager.updateAnimationSpeeds(playbackSpeed);
    }

    private void pauseAnimations() {
        pauseTime = System.currentTimeMillis();
        animationManager.pauseAnimations();
    }

    private void resumeAnimations() {
        long resumeTime = System.currentTimeMillis();
        long elapsedTime = resumeTime - pauseTime;
        animationManager.resumeAnimations(elapsedTime);
    }

    private void stopAnimations() {
        animationManager.stopAnimations();
        isAutoPlaying = false;
        isPlaying = false;
        isPaused = false;
        progressBar.setProgress(0);
        resetKeyStyles();
    }

    private void resetKeyStyles() {
        noteMap.keySet().forEach(button -> {
            button.setStyle(originalStyleMap.get(button));
            button.getStyleClass().remove("pressed");
            channel.noteOff(noteMap.get(button));
        });
    }

    private void closeResources() {
        stopAnimations();
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
    }
}
