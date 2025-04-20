package com.riccardo.pianoapp.state;

import com.riccardo.pianoapp.PianoController;

public interface PlaybackState {
    void play(PianoController pianoController);
    void pause(PianoController pianoController);
    void stop(PianoController pianoController);
}

