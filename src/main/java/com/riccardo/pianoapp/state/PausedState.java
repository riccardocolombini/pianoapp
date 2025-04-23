package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public class PausedState implements PlaybackState {
    @Override
    public void play(PianoController pianoController) {
        pianoController.changePlaybackState(new PlayingState());
        pianoController.setIsPlaying(true);
        pianoController.resumePlayback();
    }

    @Override
    public void pause(PianoController pianoController) {
        pianoController.stopPlayback();
        pianoController.pauseTime = System.currentTimeMillis();
        pianoController.animationManager.stopAnimations();
        pianoController.changePlaybackState(new PausedState());
        pianoController.setIsPlaying(false);
    }


    @Override
    public void stop(PianoController pianoController) {
        pianoController.changePlaybackState(new StoppedState());
        pianoController.setIsPlaying(false);
        pianoController.resetPlayback();
    }
}

