package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public class PausedState implements PlaybackState {
    @Override
    public void play(PianoController pianoController) {
        pianoController.changePlaybackState(new PlayingState());
        pianoController.setIsPlaying(true);
        pianoController.resumePlayback();  // Riprendi la riproduzione
    }

    @Override
    public void pause(PianoController pianoController) {
        // Ferma la riproduzione MIDI
        pianoController.stopPlayback();

        // Salva il tempo della pausa per riprendere da lì
        pianoController.pauseTime = System.currentTimeMillis();

        // Ferma le animazioni
        pianoController.animationManager.stopAnimations();

        // Cambia lo stato a PausedState
        pianoController.changePlaybackState(new PausedState());
        pianoController.setIsPlaying(false); // La riproduzione è in pausa
    }


    @Override
    public void stop(PianoController pianoController) {
        pianoController.changePlaybackState(new StoppedState());
        pianoController.setIsPlaying(false);
        pianoController.resetPlayback();
    }
}
