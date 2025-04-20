package com.riccardo.pianoapp.animation;

public interface PlaybackObserver {
    void onPlaybackStarted();
    void onPlaybackPaused();
    void onPlaybackStopped();
}
