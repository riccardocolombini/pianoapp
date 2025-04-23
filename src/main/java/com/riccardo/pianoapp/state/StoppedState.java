package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public class StoppedState implements PlaybackState {
    @Override
    public void play(PianoController pianoController) {
        pianoController.changePlaybackState(new PlayingState());
        pianoController.setIsPlaying(true);
        pianoController.startPlayback();
    }

    @Override
    public void pause(PianoController pianoController) {
    }

    @Override
    public void stop(PianoController pianoController) {
        pianoController.changePlaybackState(new StoppedState());
        pianoController.setIsPlaying(false);
        pianoController.stopPlayback();
        pianoController.animationManager.stopAnimations();
        pianoController.resetPlayback();
    }
}


