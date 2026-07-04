package com.smartfarm.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.smartfarm.util.SceneSwitcher;

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

    private Label capsLockLabel;
    private String originalButtonText;

    @FXML
    public void initialize() {
        fadeInAnimation();
        setupPasswordToggle();
        setupEnterKey();
        setupAutoFocus();
        setupCapsLockWarning();
    }

    private void setupEnterKey() {
        formPanel.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !signUpButton.isDisabled()) {
                handleSignUp();
                e.consume();
            }
        });
    }

    private void setupAutoFocus() {
        Platform.runLater(() -> nameField.requestFocus());
    }

    private void setupCapsLockWarning() {
        capsLockLabel = new Label("\u26A0 Caps Lock is ON");
        capsLockLabel.setStyle("-fx-text-fill: #E68A00; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
        capsLockLabel.setVisible(false);
        capsLockLabel.setManaged(false);

        int idx = formPanel.getChildren().indexOf(errorLabel);
        if (idx >= 0) {
            formPanel.getChildren().add(idx, capsLockLabel);
        }

        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        passwordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
        visiblePasswordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        visiblePasswordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
        confirmPasswordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        confirmPasswordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
        visibleConfirmField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        visibleConfirmField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
    }

    private void checkCapsLock(KeyEvent e) {
        boolean capsOn = java.awt.Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
        capsLockLabel.setVisible(capsOn);
        capsLockLabel.setManaged(capsOn);
    }

    private void setButtonLoading(Button button, boolean loading) {
        if (loading) {
            originalButtonText = button.getText();
            button.setDisable(true);
            button.setText("\u23F3  Please wait...");
        } else {
            button.setDisable(false);
            button.setText(originalButtonText);
        }
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
            if (name.isEmpty()) {
                shakeAnimation(nameField.getParent());
            } else if (email.isEmpty()) {
                shakeAnimation(emailField.getParent());
            } else {
                shakeAnimation(passwordField.getParent());
            }
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            shakeAnimation(confirmPasswordField.getParent());
            return;
        }

        setButtonLoading(signUpButton, true);

        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(e -> {
            setButtonLoading(signUpButton, false);
            errorLabel.setStyle("-fx-text-fill: #2E7D32;");
            errorLabel.setText("\u2714 Account created successfully!");

            PauseTransition back = new PauseTransition(Duration.millis(1500));
            back.setOnFinished(ev -> {
                errorLabel.setText("");
                errorLabel.setStyle("");
                handleGoToLogin();
            });
            back.play();
        });
        delay.play();
    }

    @FXML
    private void handleGoToLogin() {
        Stage stage = (Stage) formPanel.getScene().getWindow();
        SceneSwitcher.switchTo(stage, "/fxml/login.fxml");
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