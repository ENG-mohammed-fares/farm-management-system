package com.smartfarm.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.regex.Pattern;

import com.smartfarm.util.SceneSwitcher;
import com.smartfarm.dao.UserDAO;
import com.smartfarm.dao.FarmWorkerDAO;

public class SignUpController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @FXML private TextField nameField;

    @FXML private Label emailToggleTab;
    @FXML private Label phoneToggleTab;
    @FXML private HBox emailInputBox;
    @FXML private GridPane phoneInputBox;
    @FXML private TextField emailField;
    @FXML private ImageView flagImage;
    @FXML private TextField countryCodeField;
    @FXML private TextField phoneNumberField;

    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView passwordEyeIcon;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmField;
    @FXML private ImageView confirmEyeIcon;
    @FXML private Label errorLabel;
    @FXML private Button signUpButton;
    @FXML private VBox formPanel;

    private boolean usingEmail = true;
    private Label capsLockLabel;
    private String originalButtonText;

    private static final java.util.Map<String, String> COUNTRY_CODES = new java.util.HashMap<>();
    static {
        COUNTRY_CODES.put("211", "ss");
        COUNTRY_CODES.put("212", "ma");
        COUNTRY_CODES.put("213", "dz");
        COUNTRY_CODES.put("216", "tn");
        COUNTRY_CODES.put("218", "ly");
        COUNTRY_CODES.put("220", "gm");
        COUNTRY_CODES.put("221", "sn");
        COUNTRY_CODES.put("222", "mr");
        COUNTRY_CODES.put("223", "ml");
        COUNTRY_CODES.put("224", "gn");
        COUNTRY_CODES.put("225", "ci");
        COUNTRY_CODES.put("226", "bf");
        COUNTRY_CODES.put("227", "ne");
        COUNTRY_CODES.put("228", "tg");
        COUNTRY_CODES.put("229", "bj");
        COUNTRY_CODES.put("230", "mu");
        COUNTRY_CODES.put("231", "lr");
        COUNTRY_CODES.put("232", "sl");
        COUNTRY_CODES.put("233", "gh");
        COUNTRY_CODES.put("234", "ng");
        COUNTRY_CODES.put("235", "td");
        COUNTRY_CODES.put("236", "cf");
        COUNTRY_CODES.put("237", "cm");
        COUNTRY_CODES.put("238", "cv");
        COUNTRY_CODES.put("240", "gq");
        COUNTRY_CODES.put("241", "ga");
        COUNTRY_CODES.put("242", "cg");
        COUNTRY_CODES.put("243", "cd");
        COUNTRY_CODES.put("244", "ao");
        COUNTRY_CODES.put("245", "gw");
        COUNTRY_CODES.put("248", "sc");
        COUNTRY_CODES.put("249", "sd");
        COUNTRY_CODES.put("250", "rw");
        COUNTRY_CODES.put("251", "et");
        COUNTRY_CODES.put("252", "so");
        COUNTRY_CODES.put("253", "dj");
        COUNTRY_CODES.put("254", "ke");
        COUNTRY_CODES.put("255", "tz");
        COUNTRY_CODES.put("256", "ug");
        COUNTRY_CODES.put("257", "bi");
        COUNTRY_CODES.put("258", "mz");
        COUNTRY_CODES.put("260", "zm");
        COUNTRY_CODES.put("261", "mg");
        COUNTRY_CODES.put("263", "zw");
        COUNTRY_CODES.put("264", "na");
        COUNTRY_CODES.put("265", "mw");
        COUNTRY_CODES.put("266", "ls");
        COUNTRY_CODES.put("267", "bw");
        COUNTRY_CODES.put("268", "sz");
        COUNTRY_CODES.put("269", "km");
        COUNTRY_CODES.put("291", "er");
        COUNTRY_CODES.put("297", "aw");
        COUNTRY_CODES.put("351", "pt");
        COUNTRY_CODES.put("352", "lu");
        COUNTRY_CODES.put("353", "ie");
        COUNTRY_CODES.put("354", "is");
        COUNTRY_CODES.put("355", "al");
        COUNTRY_CODES.put("356", "mt");
        COUNTRY_CODES.put("357", "cy");
        COUNTRY_CODES.put("358", "fi");
        COUNTRY_CODES.put("359", "bg");
        COUNTRY_CODES.put("370", "lt");
        COUNTRY_CODES.put("371", "lv");
        COUNTRY_CODES.put("372", "ee");
        COUNTRY_CODES.put("373", "md");
        COUNTRY_CODES.put("374", "am");
        COUNTRY_CODES.put("375", "by");
        COUNTRY_CODES.put("376", "ad");
        COUNTRY_CODES.put("377", "mc");
        COUNTRY_CODES.put("378", "sm");
        COUNTRY_CODES.put("380", "ua");
        COUNTRY_CODES.put("381", "rs");
        COUNTRY_CODES.put("382", "me");
        COUNTRY_CODES.put("385", "hr");
        COUNTRY_CODES.put("386", "si");
        COUNTRY_CODES.put("387", "ba");
        COUNTRY_CODES.put("389", "mk");
        COUNTRY_CODES.put("420", "cz");
        COUNTRY_CODES.put("421", "sk");
        COUNTRY_CODES.put("423", "li");
        COUNTRY_CODES.put("501", "bz");
        COUNTRY_CODES.put("502", "gt");
        COUNTRY_CODES.put("503", "sv");
        COUNTRY_CODES.put("504", "hn");
        COUNTRY_CODES.put("505", "ni");
        COUNTRY_CODES.put("506", "cr");
        COUNTRY_CODES.put("507", "pa");
        COUNTRY_CODES.put("509", "ht");
        COUNTRY_CODES.put("591", "bo");
        COUNTRY_CODES.put("592", "gy");
        COUNTRY_CODES.put("593", "ec");
        COUNTRY_CODES.put("595", "py");
        COUNTRY_CODES.put("597", "sr");
        COUNTRY_CODES.put("598", "uy");
        COUNTRY_CODES.put("670", "tl");
        COUNTRY_CODES.put("673", "bn");
        COUNTRY_CODES.put("674", "nr");
        COUNTRY_CODES.put("675", "pg");
        COUNTRY_CODES.put("676", "to");
        COUNTRY_CODES.put("677", "sb");
        COUNTRY_CODES.put("678", "vu");
        COUNTRY_CODES.put("679", "fj");
        COUNTRY_CODES.put("680", "pw");
        COUNTRY_CODES.put("685", "ws");
        COUNTRY_CODES.put("686", "ki");
        COUNTRY_CODES.put("688", "tv");
        COUNTRY_CODES.put("691", "fm");
        COUNTRY_CODES.put("692", "mh");
        COUNTRY_CODES.put("850", "kp");
        COUNTRY_CODES.put("852", "hk");
        COUNTRY_CODES.put("853", "mo");
        COUNTRY_CODES.put("855", "kh");
        COUNTRY_CODES.put("856", "la");
        COUNTRY_CODES.put("880", "bd");
        COUNTRY_CODES.put("886", "tw");
        COUNTRY_CODES.put("960", "mv");
        COUNTRY_CODES.put("961", "lb");
        COUNTRY_CODES.put("962", "jo");
        COUNTRY_CODES.put("963", "sy");
        COUNTRY_CODES.put("964", "iq");
        COUNTRY_CODES.put("965", "kw");
        COUNTRY_CODES.put("966", "sa");
        COUNTRY_CODES.put("967", "ye");
        COUNTRY_CODES.put("968", "om");
        COUNTRY_CODES.put("970", "ps");
        COUNTRY_CODES.put("971", "ae");
        COUNTRY_CODES.put("972", "il");
        COUNTRY_CODES.put("973", "bh");
        COUNTRY_CODES.put("974", "qa");
        COUNTRY_CODES.put("975", "bt");
        COUNTRY_CODES.put("976", "mn");
        COUNTRY_CODES.put("977", "np");
        COUNTRY_CODES.put("992", "tj");
        COUNTRY_CODES.put("993", "tm");
        COUNTRY_CODES.put("994", "az");
        COUNTRY_CODES.put("995", "ge");
        COUNTRY_CODES.put("996", "kg");
        COUNTRY_CODES.put("998", "uz");
        COUNTRY_CODES.put("20", "eg");
        COUNTRY_CODES.put("27", "za");
        COUNTRY_CODES.put("30", "gr");
        COUNTRY_CODES.put("31", "nl");
        COUNTRY_CODES.put("32", "be");
        COUNTRY_CODES.put("33", "fr");
        COUNTRY_CODES.put("34", "es");
        COUNTRY_CODES.put("36", "hu");
        COUNTRY_CODES.put("39", "it");
        COUNTRY_CODES.put("40", "ro");
        COUNTRY_CODES.put("41", "ch");
        COUNTRY_CODES.put("43", "at");
        COUNTRY_CODES.put("44", "gb");
        COUNTRY_CODES.put("45", "dk");
        COUNTRY_CODES.put("46", "se");
        COUNTRY_CODES.put("47", "no");
        COUNTRY_CODES.put("48", "pl");
        COUNTRY_CODES.put("49", "de");
        COUNTRY_CODES.put("51", "pe");
        COUNTRY_CODES.put("52", "mx");
        COUNTRY_CODES.put("53", "cu");
        COUNTRY_CODES.put("54", "ar");
        COUNTRY_CODES.put("55", "br");
        COUNTRY_CODES.put("56", "cl");
        COUNTRY_CODES.put("57", "co");
        COUNTRY_CODES.put("58", "ve");
        COUNTRY_CODES.put("60", "my");
        COUNTRY_CODES.put("61", "au");
        COUNTRY_CODES.put("62", "id");
        COUNTRY_CODES.put("63", "ph");
        COUNTRY_CODES.put("64", "nz");
        COUNTRY_CODES.put("65", "sg");
        COUNTRY_CODES.put("66", "th");
        COUNTRY_CODES.put("81", "jp");
        COUNTRY_CODES.put("82", "kr");
        COUNTRY_CODES.put("84", "vn");
        COUNTRY_CODES.put("86", "cn");
        COUNTRY_CODES.put("90", "tr");
        COUNTRY_CODES.put("91", "in");
        COUNTRY_CODES.put("92", "pk");
        COUNTRY_CODES.put("93", "af");
        COUNTRY_CODES.put("94", "lk");
        COUNTRY_CODES.put("95", "mm");
        COUNTRY_CODES.put("98", "ir");
        COUNTRY_CODES.put("1", "us");
        COUNTRY_CODES.put("7", "kz");
    }

    @FXML
    public void initialize() {
        fadeInAnimation();
        setupPasswordToggle();
        setupPhoneFieldLimits();
        setupEnterKey();
        setupAutoFocus();
        setupCapsLockWarning();
    }

    private void setupPhoneFieldLimits() {
        countryCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 3) digits = digits.substring(0, 3);
            if (!digits.equals(newVal.replace("+", ""))) {
                countryCodeField.setText(digits);
                return;
            }
            updateFlag(digits);
        });

        phoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 9) digits = digits.substring(0, 9);
            if (!digits.equals(newVal)) {
                phoneNumberField.setText(digits);
            }
        });
    }

    private void updateFlag(String digits) {
        String iso = null;
        if (!digits.isEmpty()) {
            iso = COUNTRY_CODES.get(digits);

            if (iso == null && digits.length() < 3) {
                if (digits.length() >= 2) {
                    iso = COUNTRY_CODES.get(digits.substring(0, 2));
                }
                if (iso == null) {
                    iso = COUNTRY_CODES.get(digits.substring(0, 1));
                }
            }
        }

        if (iso != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream("/images/flags/" + iso + ".png"));
                flagImage.setImage(img);
                flagImage.setVisible(true);
            } catch (Exception ex) {
                flagImage.setVisible(false);
            }
        } else {
            flagImage.setVisible(false);
        }
    }

    @FXML
    private void handleShowEmailField() {
        usingEmail = true;
        emailInputBox.setVisible(true);
        emailInputBox.setManaged(true);
        phoneInputBox.setVisible(false);
        phoneInputBox.setManaged(false);

        emailToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
        phoneToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #999999; -fx-background-color: transparent; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
    }

    @FXML
    private void handleShowPhoneField() {
        usingEmail = false;
        emailInputBox.setVisible(false);
        emailInputBox.setManaged(false);
        phoneInputBox.setVisible(true);
        phoneInputBox.setManaged(true);

        phoneToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
        emailToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #999999; -fx-background-color: transparent; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
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
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty()) {
            errorLabel.setText("Please enter your user name");
            shakeAnimation(nameField.getParent());
            return;
        }

        String email = null;
        String phone = null;

        if (usingEmail) {
            email = emailField.getText().trim();
            if (email.isEmpty()) {
                errorLabel.setText("Please enter your email");
                shakeAnimation(emailField.getParent());
                return;
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errorLabel.setText("Email must look like: name123@domain.com");
                shakeAnimation(emailField.getParent());
                return;
            }
        } else {
            String code = countryCodeField.getText().trim();
            String number = phoneNumberField.getText().trim();

            if (code.isEmpty() || code.length() != 3) {
                errorLabel.setText("Country code must be exactly 3 digits (e.g. 970)");
                shakeAnimation(countryCodeField.getParent());
                return;
            }
            if (number.isEmpty() || number.length() != 9) {
                errorLabel.setText("Phone number must be exactly 9 digits");
                shakeAnimation(phoneNumberField.getParent());
                return;
            }
            phone = "+" + code + number;
        }

        if (password.trim().isEmpty()) {
            errorLabel.setText("Password cannot be empty or spaces only");
            shakeAnimation(passwordField.getParent());
            return;
        }

        if (password.length() < 8) {
            errorLabel.setText("Password must be at least 8 characters");
            shakeAnimation(passwordField.getParent());
            return;
        }

        // if (!password.matches(".*[A-Z].*")) {ss
        //     errorLabel.setText("Password must contain at least one uppercase letter");
        //     shakeAnimation(passwordField.getParent());
        //     return;
        // }

        // if (!password.matches(".*[a-z].*")) {
        //     errorLabel.setText("Password must contain at least one lowercase letter");
        //     shakeAnimation(passwordField.getParent());
        //     return;
        // }

        // if (!password.matches(".*\\d.*")) {
        //     errorLabel.setText("Password must contain at least one number");
        //     shakeAnimation(passwordField.getParent());
        //     return;
        // }

        // if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
        //     errorLabel.setText("Password must contain at least one special character");
        //     shakeAnimation(passwordField.getParent());
        //     return;
        // }

        if (confirm.trim().isEmpty()) {
            errorLabel.setText("Please confirm your password");
            shakeAnimation(confirmPasswordField.getParent());
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            shakeAnimation(confirmPasswordField.getParent());
            return;
        }

        setButtonLoading(signUpButton, true);

        String finalEmail = email;
        String finalPhone = phone;

        PauseTransition delay = new PauseTransition(Duration.millis(600));
        delay.setOnFinished(e -> {
            try {
                if (finalEmail != null && UserDAO.emailExists(finalEmail)) {
                    setButtonLoading(signUpButton, false);
                    errorLabel.setText("This email is already registered");
                    shakeAnimation(emailField.getParent());
                    return;
                }

                if (finalPhone != null && UserDAO.phoneExists(finalPhone)) {
                    setButtonLoading(signUpButton, false);
                    errorLabel.setText("This phone number is already registered");
                    shakeAnimation(phoneNumberField.getParent());
                    return;
                }


                int userId = UserDAO.createWorker(name, finalEmail, finalPhone, password);
                FarmWorkerDAO.assignWorker(userId, "HARVESTER", 8.0, "kg");

                setButtonLoading(signUpButton, false);
                errorLabel.setStyle("-fx-text-fill: #2E7D32;");
                errorLabel.setText("\u2714 Account created! Redirecting to login...");

                PauseTransition back = new PauseTransition(Duration.millis(1500));
                back.setOnFinished(ev -> {
                    errorLabel.setText("");
                    errorLabel.setStyle("");
                    handleGoToLogin();
                });
                back.play();

            } catch (Exception ex) {
                setButtonLoading(signUpButton, false);
                errorLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
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