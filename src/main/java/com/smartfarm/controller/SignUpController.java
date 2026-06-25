package com.smartfarm.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SignUpController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView passwordEyeIcon;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmField;
    @FXML private ImageView confirmEyeIcon;
    @FXML private Label errorLabel;
    @FXML private Button signUpButton;
    @FXML private VBox formPanel;

    @FXML
    public void initialize() {
        fadeInAnimation();
        setupPasswordToggle();
    }

    private void fadeInAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), formPanel);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(800), formPanel);
        slide.setFromY(30);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }

    private void setupPasswordToggle() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleConfirmField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
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
    private void handleShowHideConfirm() {
        if (confirmPasswordField.isVisible()) {
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            visibleConfirmField.setVisible(true);
            visibleConfirmField.setManaged(true);
            visibleConfirmField.requestFocus();
            confirmEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_open.png")));
        } else {
            visibleConfirmField.setVisible(false);
            visibleConfirmField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordField.requestFocus();
            confirmEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eye_closed.png")));
        }
    }

    @FXML
    private void handleSignUp() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            shakeAnimation(signUpButton);
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            shakeAnimation(signUpButton);
            return;
        }

        errorLabel.setText("");
    }

    @FXML
    private void handleGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) formPanel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
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
}