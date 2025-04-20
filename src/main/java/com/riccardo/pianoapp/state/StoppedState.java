package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public class StoppedState implements PlaybackState {
    @Override
    public void play(PianoController pianoController) {
        pianoController.changePlaybackState(new PlayingState());
        pianoController.setIsPlaying(true);
        pianoController.startPlayback();  // Inizia la riproduzione
    }

    @Override
    public void pause(PianoController pianoController) {
        // Non fare nulla, la riproduzione è già fermata
    }

    @Override
    public void stop(PianoController pianoController) {
        // Cambia lo stato a StoppedState
        pianoController.changePlaybackState(new StoppedState());
        pianoController.setIsPlaying(false);

        // Ferma la riproduzione MIDI
        pianoController.stopPlayback();  // Assicurati che il MIDI venga fermato

        // Ferma tutte le animazioni
        pianoController.animationManager.stopAnimations();

        // Resetta la progressione e lo stato della riproduzione
        pianoController.resetPlayback();
    }

}
