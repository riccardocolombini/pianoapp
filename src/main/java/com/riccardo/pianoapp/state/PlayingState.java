package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public class PlayingState implements PlaybackState {
    @Override
    public void play(PianoController pianoController) {
        // Non fare nulla, la riproduzione è già in corso
    }

    @Override
    public void pause(PianoController pianoController) {
        pianoController.changePlaybackState(new PausedState());
        pianoController.setIsPlaying(false);  // Ferma la riproduzione
        pianoController.resetPlayback();     // Resetta la riproduzione (opzionale)
    }

    @Override
    public void stop(PianoController pianoController) {
        pianoController.changePlaybackState(new StoppedState());
        pianoController.setIsPlaying(false);
        pianoController.resetPlayback();
    }
}