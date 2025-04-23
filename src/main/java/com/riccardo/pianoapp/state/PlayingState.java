package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public class PlayingState implements PlaybackState {
    @Override
    public void play(PianoController pianoController) {
    }

    @Override
    public void pause(PianoController pianoController) {
        pianoController.changePlaybackState(new PausedState());
        pianoController.setIsPlaying(false);
        pianoController.resetPlayback();
    }

    @Override
    public void stop(PianoController pianoController) {
        pianoController.changePlaybackState(new StoppedState());
        pianoController.setIsPlaying(false);
        pianoController.resetPlayback();
    }
}

