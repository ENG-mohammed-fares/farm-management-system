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

    @FXML private VBox loginView;
    @FXML private VBox forgotView;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView passwordEyeIcon;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML private TextField forgotEmailField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField visibleNewPasswordField;
    @FXML private ImageView newPasswordEyeIcon;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private TextField visibleConfirmNewPasswordField;
    @FXML private ImageView confirmNewPasswordEyeIcon;
    @FXML private Label forgotErrorLabel;
    @FXML private Button resetPasswordButton;

    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;
    @FXML private VBox leftPanel;
    @FXML private StackPane rightPanel;
    @FXML private Label smartFarmLabel;
    @FXML private Label greetingLabel;

    private boolean isDarkMode = false;

    @FXML
    public void initialize() {
        fadeInAnimation();
        setupEmailDetection();
        setupPasswordToggle();
        startBubblesAnimation();
        startTypewriterAnimation();
        startGreetingAnimation();
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

    private void setupEmailDetection() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.contains("@")) {
                emailField.setPromptText("Enter your email");
            } else if (newVal.matches("[+]?[0-9\\-\\s]+")) {
                emailField.setPromptText("Enter your phone");
            } else {
                emailField.setPromptText("Enter your email or phone");
            }
        });
    }

    private void setupPasswordToggle() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        visibleConfirmNewPasswordField.textProperty().bindBidirectional(confirmNewPasswordField.textProperty());
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
    private void handleForgotPassword() {
        switchView(loginView, forgotView);
    }

    @FXML
    private void handleBackToLogin() {
        switchView(forgotView, loginView);
    }

    private void switchView(VBox from, VBox to) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), from);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            from.setVisible(false);
            from.setManaged(false);

            to.setVisible(true);
            to.setManaged(true);
            to.setOpacity(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), to);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handleResetPassword() {
        String input = forgotEmailField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmNewPasswordField.getText().trim();

        if (input.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            forgotErrorLabel.setText("Please fill in all fields");
            shakeAnimation(resetPasswordButton);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            forgotErrorLabel.setText("Passwords do not match");
            shakeAnimation(resetPasswordButton);
            return;
        }

        forgotErrorLabel.setText("");
    }

    @FXML
    private void handleShowHidePassword() {
        if (passwordField.isVisible()) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.requestFocus();
            passwordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_open.png")));
        } else {
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_closed.png")));
        }
    }

    @FXML
    private void handleShowHideNewPassword() {
        if (newPasswordField.isVisible()) {
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            visibleNewPasswordField.setVisible(true);
            visibleNewPasswordField.setManaged(true);
            visibleNewPasswordField.requestFocus();
            newPasswordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_open.png")));
        } else {
            visibleNewPasswordField.setVisible(false);
            visibleNewPasswordField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            newPasswordField.requestFocus();
            newPasswordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_closed.png")));
        }
    }

    @FXML
    private void handleShowHideConfirmNewPassword() {
        if (confirmNewPasswordField.isVisible()) {
            confirmNewPasswordField.setVisible(false);
            confirmNewPasswordField.setManaged(false);
            visibleConfirmNewPasswordField.setVisible(true);
            visibleConfirmNewPasswordField.setManaged(true);
            visibleConfirmNewPasswordField.requestFocus();
            confirmNewPasswordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_open.png")));
        } else {
            visibleConfirmNewPasswordField.setVisible(false);
            visibleConfirmNewPasswordField.setManaged(false);
            confirmNewPasswordField.setVisible(true);
            confirmNewPasswordField.setManaged(true);
            confirmNewPasswordField.requestFocus();
            confirmNewPasswordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_closed.png")));
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