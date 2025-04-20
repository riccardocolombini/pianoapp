package com.riccardo.pianoapp.midi;

import javax.sound.midi.*;

/**
 * Adapter for MIDI operations.
 */
public class MidiManager {

    private final Sequencer sequencer;
    private final Synthesizer synthesizer;
    private final MidiChannel midiChannel;
    private long currentTick = 0;  // Variabile per tenere traccia della posizione corrente

    public MidiManager(Synthesizer synthesizer, MidiChannel channel) {
        this.synthesizer = synthesizer;
        this.midiChannel = channel;
        try {
            this.sequencer = MidiSystem.getSequencer();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException("Sequencer not available", e);
        }
    }

    public void playMidi(Sequence sequence, double playbackSpeed) {
        try {
            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM((float) (120 * playbackSpeed));
            sequencer.open();
            sequencer.start();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void resumePlayback(long elapsedTime) {
        try {
            sequencer.setTickPosition(currentTick);  // Ripristina la posizione del tick
            sequencer.setTempoFactor(1);  // Riprendi la velocità normale
            sequencer.start();  // Avvia la riproduzione dal punto salvato
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getBpmFromMidi(Sequence sequence) {
        int bpm = 120;
        try {
            for (Track track : sequence.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    if (event.getMessage() instanceof MetaMessage) {
                        MetaMessage metaMessage = (MetaMessage) event.getMessage();
                        if (metaMessage.getType() == 81) {
                            byte[] data = metaMessage.getData();
                            int microsecondsPerQuarterNote = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            bpm = (int) (60000000.0 / microsecondsPerQuarterNote);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bpm;
    }

    public int countTotalNotes(Sequence sequence) {
        int totalNotes = 0;
        try {
            for (Track track : sequence.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    if (event.getMessage() instanceof ShortMessage) {
                        ShortMessage msg = (ShortMessage) event.getMessage();
                        if (msg.getCommand() == ShortMessage.NOTE_ON) {
                            totalNotes++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalNotes;
    }

    public void stopMidi() {
        // Ferma il sequencer e memorizza la posizione corrente
        currentTick = sequencer.getTickPosition();
        sequencer.stop();
    }

    public void pauseMidi() {
        // Ferma la riproduzione, ma memorizza la posizione corrente per poterla riprendere
        currentTick = sequencer.getTickPosition();
        sequencer.setTempoFactor(0); // Ferma la riproduzione
    }

    public void resumeMidi() {
        // Riprende la riproduzione dal punto in cui è stata fermata
        if (sequencer != null) {
            sequencer.setTickPosition(currentTick); // Ripristina la posizione del tick
            sequencer.setTempoFactor(1); // Ripristina la velocità di riproduzione
            sequencer.start(); // Riprende la riproduzione
        }
    }

    public long getCurrentTick() {
        return currentTick;  // Ottieni la posizione corrente della riproduzione
    }
}
