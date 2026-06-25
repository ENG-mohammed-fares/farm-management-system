package com.smartfarm.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;
    @FXML private VBox leftPanel;
    @FXML private StackPane rightPanel;
    @FXML private Label smartFarmLabel;
    @FXML private Label greetingLabel;
    @FXML private Label projectNameLabel;
    @FXML private Label statsLabel;

    private boolean isDarkMode = false;

    @FXML
    public void initialize() {
        fadeInAnimation();
        setupEmailDetection();
        setupPasswordToggle();
        startBubblesAnimation();
        startTypewriterAnimation();
        startGreetingAnimation();
        startProjectNameAnimation();
        startStatsAnimation();
    }

    private void fadeInAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), leftPanel);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), leftPanel);
        slide.setFromX(-30);
        slide.setToX(0);

        new ParallelTransition(fade, slide).play();
    }

    private void startBubblesAnimation() {
        for (int i = 0; i < 8; i++) {
            double size = 10 + Math.random() * 30;
            Circle bubble = new Circle(size);
            bubble.setFill(Color.rgb(255, 255, 255, 0.08 + Math.random() * 0.1));

            double x = Math.random() * 520 - 260;
            double startY = 200 + Math.random() * 100;

            bubble.setTranslateX(x);
            bubble.setTranslateY(startY);

            rightPanel.getChildren().add(0, bubble);

            TranslateTransition move = new TranslateTransition(
                    Duration.millis(3000 + Math.random() * 3000), bubble);
            move.setFromY(startY);
            move.setToY(-300);
            move.setCycleCount(Animation.INDEFINITE);
            move.setDelay(Duration.millis(Math.random() * 3000));

            FadeTransition fade = new FadeTransition(
                    Duration.millis(3000 + Math.random() * 3000), bubble);
            fade.setFromValue(0.2);
            fade.setToValue(0);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setDelay(Duration.millis(Math.random() * 3000));

            move.play();
            fade.play();
        }
    }

    private void startTypewriterAnimation() {
        String fullText = "Smart Farm Management";
        smartFarmLabel.setText("");

        Timeline timeline = new Timeline();
        for (int i = 0; i < fullText.length(); i++) {
            final int index = i;
            KeyFrame frame = new KeyFrame(Duration.millis(80 * i), e ->
                    smartFarmLabel.setText(fullText.substring(0, index + 1))
            );
            timeline.getKeyFrames().add(frame);
        }
        timeline.setDelay(Duration.millis(500));
        timeline.play();
    }

    private void startGreetingAnimation() {
        int hour = java.time.LocalTime.now().getHour();
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "🌅 Good Morning!";
        } else if (hour >= 12 && hour < 17) {
            greeting = "☀️ Good Afternoon!";
        } else if (hour >= 17 && hour < 21) {
            greeting = "🌆 Good Evening!";
        } else {
            greeting = "🌙 Good Night!";
        }

        greetingLabel.setText(greeting);
        greetingLabel.setOpacity(0);
        greetingLabel.setScaleX(0.5);
        greetingLabel.setScaleY(0.5);

        FadeTransition fade = new FadeTransition(Duration.millis(600), greetingLabel);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(600), greetingLabel);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1);
        scale.setToY(1);

        new ParallelTransition(fade, scale).play();
    }

    private void startProjectNameAnimation() {
        String fullText = "🌿 Smart Farm";
        projectNameLabel.setText("");

        Timeline timeline = new Timeline();
        for (int i = 0; i < fullText.length(); i++) {
            final int index = i;
            KeyFrame frame = new KeyFrame(Duration.millis(500 + 80 * i), e ->
                    projectNameLabel.setText(fullText.substring(0, index + 1))
            );
            timeline.getKeyFrames().add(frame);
        }
        timeline.play();
    }

    private void startStatsAnimation() {
        statsLabel.setOpacity(0);
        statsLabel.setTranslateY(20);
        statsLabel.setText("🌱 Join 10,000+ farmers\n   managing smarter");

        FadeTransition fade = new FadeTransition(Duration.millis(500), statsLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(1800));

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), statsLabel);
        slide.setFromY(20);
        slide.setToY(0);
        slide.setDelay(Duration.millis(1800));

        new ParallelTransition(fade, slide).play();
    }

    private void setupEmailDetection() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.contains("@")) {
                emailField.setPromptText("📧 Enter your email");
            } else if (newVal.matches("[0-9]+")) {
                emailField.setPromptText("📱 Enter your phone");
            } else {
                emailField.setPromptText("Enter your email or phone");
            }
        });
    }

    private void setupPasswordToggle() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleLogin() {
        String input = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (input.isEmpty() || password.isEmpty()) {
            shakeAnimation(errorLabel);
            errorLabel.setText("Please fill in all fields");
            shakeAnimation(emailField.getParent() != null ? emailField : loginButton);
            return;
        }

        errorLabel.setText("");
    }

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowHidePassword() {
        if (passwordField.isVisible()) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.requestFocus();
        } else {
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
        }
    }

    private void shakeAnimation(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    @FXML
    private void handleThemeToggle() {
        RotateTransition rotate = new RotateTransition(Duration.millis(300), themeToggleBtn);
        rotate.setByAngle(360);
        rotate.play();

        isDarkMode = !isDarkMode;

        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            themeToggleBtn.getScene().getRoot().getStyleClass().add("dark-mode");
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            themeToggleBtn.getScene().getRoot().getStyleClass().remove("dark-mode");
        }
    }
}