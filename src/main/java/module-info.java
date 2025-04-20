module com.riccardo.pianomidi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.riccardo.pianoapp to javafx.fxml;
    exports com.riccardo.pianoapp;
    exports com.riccardo.pianoapp.animation;
    opens com.riccardo.pianoapp.animation to javafx.fxml;
    exports com.riccardo.pianoapp.midi;
    opens com.riccardo.pianoapp.midi to javafx.fxml;
    exports com.riccardo.pianoapp.recording;
    opens com.riccardo.pianoapp.recording to javafx.fxml;
    exports com.riccardo.pianoapp.sound;
    opens com.riccardo.pianoapp.sound to javafx.fxml;
    exports com.riccardo.pianoapp.state;
    opens com.riccardo.pianoapp.state to javafx.fxml;
}