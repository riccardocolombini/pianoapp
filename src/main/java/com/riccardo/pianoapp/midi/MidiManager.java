package com.riccardo.pianoapp.midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

/**
 * Manages MIDI sequences and events.
 */
public class MidiManager {
    private final Synthesizer synthesizer;
    private final MidiChannel channel;

    /**
     * Constructor for MidiManager.
     *
     * @param synthesizer The synthesizer to be used.
     * @param channel     The MIDI channel to be used.
     */
    public MidiManager(Synthesizer synthesizer, MidiChannel channel) {
        this.synthesizer = synthesizer;
        this.channel = channel;
    }

    /**
     * Loads a MIDI file and returns the sequence.
     *
     * @param file The MIDI file to be loaded.
     * @return The loaded MIDI sequence.
     * @throws InvalidMidiDataException If the MIDI data is invalid.
     * @throws IOException              If an I/O error occurs.
     */
    public Sequence loadMidiFile(File file) throws InvalidMidiDataException, IOException {
        return MidiSystem.getSequence(file);
    }

    /**
     * Extracts the BPM from the MIDI sequence.
     *
     * @param sequence The MIDI sequence.
     * @return The BPM value.
     */
    public int getBpmFromMidi(Sequence sequence) {
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getMessage() instanceof MetaMessage mm) {
                    if (mm.getType() == 0x51) {
                        byte[] data = mm.getData();
                        int tempo = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                        return 60000000 / tempo;
                    }
                }
            }
        }
        return 120;
    }

    /**
     * Counts the total number of notes in the MIDI sequence.
     *
     * @param sequence The MIDI sequence.
     * @return The total number of notes.
     */
    public int countTotalNotes(Sequence sequence) {
        int totalNotes = 0;
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getMessage() instanceof ShortMessage sm) {
                    if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        totalNotes++;
                    }
                }
            }
        }
        return totalNotes;
    }
}
