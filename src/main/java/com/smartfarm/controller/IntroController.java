package com.smartfarm.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.smartfarm.util.SceneSwitcher;

public class IntroController {

    @FXML
    private Label introTitle;

    @FXML
    public void initialize() {
        startTitleAnimation();
        scheduleTransitionToLogin();
    }

    private void startTitleAnimation() {
        String fullText = "Smart Farm Management";
        introTitle.setText("");

        Timeline timeline = new Timeline();
        for (int i = 0; i < fullText.length(); i++) {
            final int index = i;
            KeyFrame frame = new KeyFrame(Duration.millis(60 * i), e ->
                    introTitle.setText(fullText.substring(0, index + 1))
            );
            timeline.getKeyFrames().add(frame);
        }
        timeline.play();
    }

    private void scheduleTransitionToLogin() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            Stage stage = (Stage) introTitle.getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/fxml/login.fxml");
        });
        pause.play();
    }
}