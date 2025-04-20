package com.riccardo.pianoapp.sound;

public interface NoteListener {
    void onNoteOn(int note);
    void onNoteOff(int note, long duration);
}

