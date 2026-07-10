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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.smartfarm.dao.UserDAO;
import com.smartfarm.dao.FarmWorkerDAO;
import com.smartfarm.util.SceneSwitcher;
import com.smartfarm.util.SessionManager;
import com.smartfarm.util.VerificationService;

import java.sql.SQLException;

public class LoginController {

    @FXML private VBox loginView;
    @FXML private VBox forgotView;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView passwordEyeIcon;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML private Label loginEmailToggleTab;
    @FXML private Label loginPhoneToggleTab;
    @FXML private HBox loginEmailInputBox;
    @FXML private GridPane loginPhoneInputBox;
    @FXML private ImageView loginFlagImage;
    @FXML private TextField loginCountryCodeField;
    @FXML private TextField loginPhoneNumberField;

    @FXML private Label forgotEmailToggleTab;
    @FXML private Label forgotPhoneToggleTab;
    @FXML private HBox forgotEmailInputBox;
    @FXML private GridPane forgotPhoneInputBox;
    @FXML private TextField forgotEmailField;
    @FXML private ImageView forgotFlagImage;
    @FXML private TextField forgotCountryCodeField;
    @FXML private TextField forgotPhoneNumberField;

    @FXML private PasswordField newPasswordField;
    @FXML private TextField visibleNewPasswordField;
    @FXML private ImageView newPasswordEyeIcon;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private TextField visibleConfirmNewPasswordField;
    @FXML private ImageView confirmNewPasswordEyeIcon;
    @FXML private Label forgotErrorLabel;
    @FXML private Button resetPasswordButton;

    @FXML private VBox verificationBox;
    @FXML private Label verificationInfoLabel;
    @FXML private TextField code1;
    @FXML private TextField code2;
    @FXML private TextField code3;
    @FXML private TextField code4;
    @FXML private TextField code5;
    @FXML private Label countdownLabel;
    @FXML private Hyperlink resendLink;
    @FXML private Button confirmCodeButton;

    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;
    @FXML private javafx.scene.layout.AnchorPane rootPane;
    @FXML private VBox leftPanel;
    @FXML private StackPane rightPanel;
    @FXML private Label smartFarmLabel;
    @FXML private Label greetingLabel;

    private boolean forgotUsingEmail = true;
    private boolean loginUsingEmail = true;

    private boolean isDarkMode = false;
    private Label capsLockLabel;
    private String loginButtonOriginalText;

    private static final java.util.Map<String, String> FORGOT_COUNTRY_CODES = new java.util.HashMap<>();
    static {
        FORGOT_COUNTRY_CODES.put("211", "ss");
        FORGOT_COUNTRY_CODES.put("212", "ma");
        FORGOT_COUNTRY_CODES.put("213", "dz");
        FORGOT_COUNTRY_CODES.put("216", "tn");
        FORGOT_COUNTRY_CODES.put("218", "ly");
        FORGOT_COUNTRY_CODES.put("220", "gm");
        FORGOT_COUNTRY_CODES.put("221", "sn");
        FORGOT_COUNTRY_CODES.put("222", "mr");
        FORGOT_COUNTRY_CODES.put("223", "ml");
        FORGOT_COUNTRY_CODES.put("224", "gn");
        FORGOT_COUNTRY_CODES.put("225", "ci");
        FORGOT_COUNTRY_CODES.put("226", "bf");
        FORGOT_COUNTRY_CODES.put("227", "ne");
        FORGOT_COUNTRY_CODES.put("228", "tg");
        FORGOT_COUNTRY_CODES.put("229", "bj");
        FORGOT_COUNTRY_CODES.put("230", "mu");
        FORGOT_COUNTRY_CODES.put("231", "lr");
        FORGOT_COUNTRY_CODES.put("232", "sl");
        FORGOT_COUNTRY_CODES.put("233", "gh");
        FORGOT_COUNTRY_CODES.put("234", "ng");
        FORGOT_COUNTRY_CODES.put("235", "td");
        FORGOT_COUNTRY_CODES.put("236", "cf");
        FORGOT_COUNTRY_CODES.put("237", "cm");
        FORGOT_COUNTRY_CODES.put("238", "cv");
        FORGOT_COUNTRY_CODES.put("240", "gq");
        FORGOT_COUNTRY_CODES.put("241", "ga");
        FORGOT_COUNTRY_CODES.put("242", "cg");
        FORGOT_COUNTRY_CODES.put("243", "cd");
        FORGOT_COUNTRY_CODES.put("244", "ao");
        FORGOT_COUNTRY_CODES.put("245", "gw");
        FORGOT_COUNTRY_CODES.put("248", "sc");
        FORGOT_COUNTRY_CODES.put("249", "sd");
        FORGOT_COUNTRY_CODES.put("250", "rw");
        FORGOT_COUNTRY_CODES.put("251", "et");
        FORGOT_COUNTRY_CODES.put("252", "so");
        FORGOT_COUNTRY_CODES.put("253", "dj");
        FORGOT_COUNTRY_CODES.put("254", "ke");
        FORGOT_COUNTRY_CODES.put("255", "tz");
        FORGOT_COUNTRY_CODES.put("256", "ug");
        FORGOT_COUNTRY_CODES.put("257", "bi");
        FORGOT_COUNTRY_CODES.put("258", "mz");
        FORGOT_COUNTRY_CODES.put("260", "zm");
        FORGOT_COUNTRY_CODES.put("261", "mg");
        FORGOT_COUNTRY_CODES.put("263", "zw");
        FORGOT_COUNTRY_CODES.put("264", "na");
        FORGOT_COUNTRY_CODES.put("265", "mw");
        FORGOT_COUNTRY_CODES.put("266", "ls");
        FORGOT_COUNTRY_CODES.put("267", "bw");
        FORGOT_COUNTRY_CODES.put("268", "sz");
        FORGOT_COUNTRY_CODES.put("269", "km");
        FORGOT_COUNTRY_CODES.put("291", "er");
        FORGOT_COUNTRY_CODES.put("297", "aw");
        FORGOT_COUNTRY_CODES.put("351", "pt");
        FORGOT_COUNTRY_CODES.put("352", "lu");
        FORGOT_COUNTRY_CODES.put("353", "ie");
        FORGOT_COUNTRY_CODES.put("354", "is");
        FORGOT_COUNTRY_CODES.put("355", "al");
        FORGOT_COUNTRY_CODES.put("356", "mt");
        FORGOT_COUNTRY_CODES.put("357", "cy");
        FORGOT_COUNTRY_CODES.put("358", "fi");
        FORGOT_COUNTRY_CODES.put("359", "bg");
        FORGOT_COUNTRY_CODES.put("370", "lt");
        FORGOT_COUNTRY_CODES.put("371", "lv");
        FORGOT_COUNTRY_CODES.put("372", "ee");
        FORGOT_COUNTRY_CODES.put("373", "md");
        FORGOT_COUNTRY_CODES.put("374", "am");
        FORGOT_COUNTRY_CODES.put("375", "by");
        FORGOT_COUNTRY_CODES.put("376", "ad");
        FORGOT_COUNTRY_CODES.put("377", "mc");
        FORGOT_COUNTRY_CODES.put("378", "sm");
        FORGOT_COUNTRY_CODES.put("380", "ua");
        FORGOT_COUNTRY_CODES.put("381", "rs");
        FORGOT_COUNTRY_CODES.put("382", "me");
        FORGOT_COUNTRY_CODES.put("385", "hr");
        FORGOT_COUNTRY_CODES.put("386", "si");
        FORGOT_COUNTRY_CODES.put("387", "ba");
        FORGOT_COUNTRY_CODES.put("389", "mk");
        FORGOT_COUNTRY_CODES.put("420", "cz");
        FORGOT_COUNTRY_CODES.put("421", "sk");
        FORGOT_COUNTRY_CODES.put("423", "li");
        FORGOT_COUNTRY_CODES.put("501", "bz");
        FORGOT_COUNTRY_CODES.put("502", "gt");
        FORGOT_COUNTRY_CODES.put("503", "sv");
        FORGOT_COUNTRY_CODES.put("504", "hn");
        FORGOT_COUNTRY_CODES.put("505", "ni");
        FORGOT_COUNTRY_CODES.put("506", "cr");
        FORGOT_COUNTRY_CODES.put("507", "pa");
        FORGOT_COUNTRY_CODES.put("509", "ht");
        FORGOT_COUNTRY_CODES.put("591", "bo");
        FORGOT_COUNTRY_CODES.put("592", "gy");
        FORGOT_COUNTRY_CODES.put("593", "ec");
        FORGOT_COUNTRY_CODES.put("595", "py");
        FORGOT_COUNTRY_CODES.put("597", "sr");
        FORGOT_COUNTRY_CODES.put("598", "uy");
        FORGOT_COUNTRY_CODES.put("670", "tl");
        FORGOT_COUNTRY_CODES.put("673", "bn");
        FORGOT_COUNTRY_CODES.put("674", "nr");
        FORGOT_COUNTRY_CODES.put("675", "pg");
        FORGOT_COUNTRY_CODES.put("676", "to");
        FORGOT_COUNTRY_CODES.put("677", "sb");
        FORGOT_COUNTRY_CODES.put("678", "vu");
        FORGOT_COUNTRY_CODES.put("679", "fj");
        FORGOT_COUNTRY_CODES.put("680", "pw");
        FORGOT_COUNTRY_CODES.put("685", "ws");
        FORGOT_COUNTRY_CODES.put("686", "ki");
        FORGOT_COUNTRY_CODES.put("688", "tv");
        FORGOT_COUNTRY_CODES.put("691", "fm");
        FORGOT_COUNTRY_CODES.put("692", "mh");
        FORGOT_COUNTRY_CODES.put("850", "kp");
        FORGOT_COUNTRY_CODES.put("852", "hk");
        FORGOT_COUNTRY_CODES.put("853", "mo");
        FORGOT_COUNTRY_CODES.put("855", "kh");
        FORGOT_COUNTRY_CODES.put("856", "la");
        FORGOT_COUNTRY_CODES.put("880", "bd");
        FORGOT_COUNTRY_CODES.put("886", "tw");
        FORGOT_COUNTRY_CODES.put("960", "mv");
        FORGOT_COUNTRY_CODES.put("961", "lb");
        FORGOT_COUNTRY_CODES.put("962", "jo");
        FORGOT_COUNTRY_CODES.put("963", "sy");
        FORGOT_COUNTRY_CODES.put("964", "iq");
        FORGOT_COUNTRY_CODES.put("965", "kw");
        FORGOT_COUNTRY_CODES.put("966", "sa");
        FORGOT_COUNTRY_CODES.put("967", "ye");
        FORGOT_COUNTRY_CODES.put("968", "om");
        FORGOT_COUNTRY_CODES.put("970", "ps");
        FORGOT_COUNTRY_CODES.put("971", "ae");
        FORGOT_COUNTRY_CODES.put("972", "il");
        FORGOT_COUNTRY_CODES.put("973", "bh");
        FORGOT_COUNTRY_CODES.put("974", "qa");
        FORGOT_COUNTRY_CODES.put("975", "bt");
        FORGOT_COUNTRY_CODES.put("976", "mn");
        FORGOT_COUNTRY_CODES.put("977", "np");
        FORGOT_COUNTRY_CODES.put("992", "tj");
        FORGOT_COUNTRY_CODES.put("993", "tm");
        FORGOT_COUNTRY_CODES.put("994", "az");
        FORGOT_COUNTRY_CODES.put("995", "ge");
        FORGOT_COUNTRY_CODES.put("996", "kg");
        FORGOT_COUNTRY_CODES.put("998", "uz");
        FORGOT_COUNTRY_CODES.put("20", "eg");
        FORGOT_COUNTRY_CODES.put("27", "za");
        FORGOT_COUNTRY_CODES.put("30", "gr");
        FORGOT_COUNTRY_CODES.put("31", "nl");
        FORGOT_COUNTRY_CODES.put("32", "be");
        FORGOT_COUNTRY_CODES.put("33", "fr");
        FORGOT_COUNTRY_CODES.put("34", "es");
        FORGOT_COUNTRY_CODES.put("36", "hu");
        FORGOT_COUNTRY_CODES.put("39", "it");
        FORGOT_COUNTRY_CODES.put("40", "ro");
        FORGOT_COUNTRY_CODES.put("41", "ch");
        FORGOT_COUNTRY_CODES.put("43", "at");
        FORGOT_COUNTRY_CODES.put("44", "gb");
        FORGOT_COUNTRY_CODES.put("45", "dk");
        FORGOT_COUNTRY_CODES.put("46", "se");
        FORGOT_COUNTRY_CODES.put("47", "no");
        FORGOT_COUNTRY_CODES.put("48", "pl");
        FORGOT_COUNTRY_CODES.put("49", "de");
        FORGOT_COUNTRY_CODES.put("51", "pe");
        FORGOT_COUNTRY_CODES.put("52", "mx");
        FORGOT_COUNTRY_CODES.put("53", "cu");
        FORGOT_COUNTRY_CODES.put("54", "ar");
        FORGOT_COUNTRY_CODES.put("55", "br");
        FORGOT_COUNTRY_CODES.put("56", "cl");
        FORGOT_COUNTRY_CODES.put("57", "co");
        FORGOT_COUNTRY_CODES.put("58", "ve");
        FORGOT_COUNTRY_CODES.put("60", "my");
        FORGOT_COUNTRY_CODES.put("61", "au");
        FORGOT_COUNTRY_CODES.put("62", "id");
        FORGOT_COUNTRY_CODES.put("63", "ph");
        FORGOT_COUNTRY_CODES.put("64", "nz");
        FORGOT_COUNTRY_CODES.put("65", "sg");
        FORGOT_COUNTRY_CODES.put("66", "th");
        FORGOT_COUNTRY_CODES.put("81", "jp");
        FORGOT_COUNTRY_CODES.put("82", "kr");
        FORGOT_COUNTRY_CODES.put("84", "vn");
        FORGOT_COUNTRY_CODES.put("86", "cn");
        FORGOT_COUNTRY_CODES.put("90", "tr");
        FORGOT_COUNTRY_CODES.put("91", "in");
        FORGOT_COUNTRY_CODES.put("92", "pk");
        FORGOT_COUNTRY_CODES.put("93", "af");
        FORGOT_COUNTRY_CODES.put("94", "lk");
        FORGOT_COUNTRY_CODES.put("95", "mm");
        FORGOT_COUNTRY_CODES.put("98", "ir");
        FORGOT_COUNTRY_CODES.put("1", "us");
        FORGOT_COUNTRY_CODES.put("7", "kz");
    }

    @FXML
    public void initialize() {
        syncThemeState();
        fadeInAnimation();
        setupEmailDetection();
        setupPasswordToggle();
        startBubblesAnimation();
        startTypewriterAnimation();
        startGreetingAnimation();
        setupEnterKey();
        setupAutoFocus();
        setupCapsLockWarning();
        setupForgotPhoneFieldLimits();
        setupLoginPhoneFieldLimits();
        setupCodeBoxAutoAdvance();
    }

    private void setupForgotPhoneFieldLimits() {
        forgotCountryCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 3) digits = digits.substring(0, 3);
            if (!digits.equals(newVal.replace("+", ""))) {
                forgotCountryCodeField.setText(digits);
                return;
            }
            updateForgotFlag(digits);
        });

        forgotPhoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 9) digits = digits.substring(0, 9);
            if (!digits.equals(newVal)) {
                forgotPhoneNumberField.setText(digits);
            }
        });
    }

    private void updateForgotFlag(String digits) {
        String iso = null;
        if (!digits.isEmpty()) {
            iso = FORGOT_COUNTRY_CODES.get(digits);

            if (iso == null && digits.length() < 3) {
                if (digits.length() >= 2) {
                    iso = FORGOT_COUNTRY_CODES.get(digits.substring(0, 2));
                }
                if (iso == null) {
                    iso = FORGOT_COUNTRY_CODES.get(digits.substring(0, 1));
                }
            }
        }

        if (iso != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream("/images/flags/" + iso + ".png"));
                forgotFlagImage.setImage(img);
                forgotFlagImage.setVisible(true);
            } catch (Exception ex) {
                forgotFlagImage.setVisible(false);
            }
        } else {
            forgotFlagImage.setVisible(false);
        }
    }

    @FXML
    private void handleShowForgotEmailField() {
        forgotUsingEmail = true;
        forgotEmailInputBox.setVisible(true);
        forgotEmailInputBox.setManaged(true);
        forgotPhoneInputBox.setVisible(false);
        forgotPhoneInputBox.setManaged(false);

        forgotEmailToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
        forgotPhoneToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #999999; -fx-background-color: transparent; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
    }

    @FXML
    private void handleShowForgotPhoneField() {
        forgotUsingEmail = false;
        forgotEmailInputBox.setVisible(false);
        forgotEmailInputBox.setManaged(false);
        forgotPhoneInputBox.setVisible(true);
        forgotPhoneInputBox.setManaged(true);

        forgotPhoneToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
        forgotEmailToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #999999; -fx-background-color: transparent; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
    }

    private void setupLoginPhoneFieldLimits() {
        loginCountryCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 3) digits = digits.substring(0, 3);
            if (!digits.equals(newVal.replace("+", ""))) {
                loginCountryCodeField.setText(digits);
                return;
            }
            updateLoginFlag(digits);
        });

        loginPhoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 9) digits = digits.substring(0, 9);
            if (!digits.equals(newVal)) {
                loginPhoneNumberField.setText(digits);
            }
        });
    }

    private void updateLoginFlag(String digits) {
        String iso = null;
        if (!digits.isEmpty()) {
            iso = FORGOT_COUNTRY_CODES.get(digits);

            if (iso == null && digits.length() < 3) {
                if (digits.length() >= 2) {
                    iso = FORGOT_COUNTRY_CODES.get(digits.substring(0, 2));
                }
                if (iso == null) {
                    iso = FORGOT_COUNTRY_CODES.get(digits.substring(0, 1));
                }
            }
        }

        if (iso != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream("/images/flags/" + iso + ".png"));
                loginFlagImage.setImage(img);
                loginFlagImage.setVisible(true);
            } catch (Exception ex) {
                loginFlagImage.setVisible(false);
            }
        } else {
            loginFlagImage.setVisible(false);
        }
    }

    @FXML
    private void handleShowLoginEmailField() {
        loginUsingEmail = true;
        loginEmailInputBox.setVisible(true);
        loginEmailInputBox.setManaged(true);
        loginPhoneInputBox.setVisible(false);
        loginPhoneInputBox.setManaged(false);

        loginEmailToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
        loginPhoneToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #999999; -fx-background-color: transparent; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
    }

    @FXML
    private void handleShowLoginPhoneField() {
        loginUsingEmail = false;
        loginEmailInputBox.setVisible(false);
        loginEmailInputBox.setManaged(false);
        loginPhoneInputBox.setVisible(true);
        loginPhoneInputBox.setManaged(true);

        loginPhoneToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
        loginEmailToggleTab.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #999999; -fx-background-color: transparent; -fx-padding: 5 14 5 14; -fx-background-radius: 14; -fx-cursor: hand;");
    }

    private void syncThemeState() {
        isDarkMode = SceneSwitcher.isDarkMode();
        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            if (!rootPane.getStyleClass().contains("dark-mode")) {
                rootPane.getStyleClass().add("dark-mode");
            }
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            rootPane.getStyleClass().remove("dark-mode");
        }
    }

    private void setupEnterKey() {
        loginView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !loginButton.isDisabled()) {
                handleLogin();
                e.consume();
            }
        });

        forgotView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !resetPasswordButton.isDisabled()) {
                handleResetPassword();
                e.consume();
            }
        });
    }

    private void setupAutoFocus() {
        Platform.runLater(() -> emailField.requestFocus());
    }

    private void setupCapsLockWarning() {
        capsLockLabel = new Label("\u26A0 Caps Lock is ON");
        capsLockLabel.setStyle("-fx-text-fill: #E68A00; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
        capsLockLabel.setVisible(false);
        capsLockLabel.setManaged(false);

        int idx = loginView.getChildren().indexOf(errorLabel);
        if (idx >= 0) {
            loginView.getChildren().add(idx, capsLockLabel);
        }

        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        passwordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
        visiblePasswordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        visiblePasswordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
        newPasswordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        newPasswordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
        confirmNewPasswordField.addEventHandler(KeyEvent.KEY_PRESSED, this::checkCapsLock);
        confirmNewPasswordField.addEventHandler(KeyEvent.KEY_RELEASED, this::checkCapsLock);
    }

    private void checkCapsLock(KeyEvent e) {
        boolean capsOn = java.awt.Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
        capsLockLabel.setVisible(capsOn);
        capsLockLabel.setManaged(capsOn);
    }

    private void setButtonLoading(Button button, boolean loading) {
        if (loading) {
            loginButtonOriginalText = button.getText();
            button.setDisable(true);
            button.setText("⏳  Please wait...");
        } else {
            button.setDisable(false);
            button.setText(loginButtonOriginalText);
        }
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
            greeting = "\u2600 Good Morning!";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon!";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening!";
        } else {
            greeting = "Good Night!";
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
        emailField.setPromptText("Email or username");
    }

    private void setupPasswordToggle() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        visibleConfirmNewPasswordField.textProperty().bindBidirectional(confirmNewPasswordField.textProperty());
    }

    @FXML
    private void handleLogin() {
        String input;

        if (loginUsingEmail) {
            input = emailField.getText().trim();
            if (input.isEmpty()) {
                errorLabel.setText("Please enter your email or username");
                shakeAnimation(emailField.getParent());
                return;
            }
        } else {
            String code = loginCountryCodeField.getText().trim();
            String number = loginPhoneNumberField.getText().trim();

            if (code.isEmpty() || code.length() != 3) {
                errorLabel.setText("Country code must be exactly 3 digits (e.g. 970)");
                shakeAnimation(loginCountryCodeField.getParent());
                return;
            }
            if (number.isEmpty() || number.length() != 9) {
                errorLabel.setText("Phone number must be exactly 9 digits");
                shakeAnimation(loginPhoneNumberField.getParent());
                return;
            }
            input = "+" + code + number;
        }

        String password = passwordField.getText().trim();

        if (password.isEmpty()) {
            errorLabel.setText("Please enter your password");
            shakeAnimation(passwordField.getParent());
            return;
        }

        setButtonLoading(loginButton, true);

        String finalInput = input;

        PauseTransition delay = new PauseTransition(Duration.millis(600));
        delay.setOnFinished(e -> {
            try {
                int[] result = UserDAO.login(finalInput, password);
                if (result == null) {
                    setButtonLoading(loginButton, false);
                    errorLabel.setText("Invalid email/phone or password");
                    shakeAnimation(loginButton);
                    return;
                }

                int userId = result[0];
                boolean isAdmin = result[1] == 1;
                String name = UserDAO.getUserName(userId);

                Stage stage = (Stage) loginButton.getScene().getWindow();

                if (isAdmin) {
                    SessionManager.login(userId, name, true);
                    if (!SceneSwitcher.switchTo(stage, "/fxml/dashboard.fxml")) {
                        SessionManager.logout();
                        setButtonLoading(loginButton, false);
                        errorLabel.setText("Could not open admin dashboard: " + SceneSwitcher.getLastErrorMessage());
                    }
                } else {
                    int fwId = FarmWorkerDAO.getFwId(userId);
                    if (fwId == -1) {
                        setButtonLoading(loginButton, false);
                        errorLabel.setText("This account is not assigned as an active worker");
                        return;
                    }
                    SessionManager.login(userId, name, false);
                    SessionManager.setFwId(fwId);
                    if (!SceneSwitcher.switchTo(stage, "/fxml/worker_dashboard.fxml")) {
                        SessionManager.logout();
                        setButtonLoading(loginButton, false);
                        errorLabel.setText("Could not open worker dashboard: " + SceneSwitcher.getLastErrorMessage());
                    }
                }

            } catch (SQLException ex) {
                setButtonLoading(loginButton, false);
                errorLabel.setText("Database error: " + ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex) {
                setButtonLoading(loginButton, false);
                errorLabel.setText("Connection error");
                ex.printStackTrace();
            }
        });
        delay.play();
    }

    @FXML
    private void handleSignUp() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchTo(stage, "/fxml/signup.fxml");
    }

    @FXML
    private void handleForgotPassword() {
        switchView(loginView, forgotView);
        Platform.runLater(() -> forgotEmailField.requestFocus());
    }

    @FXML
    private void handleBackToLogin() {
        switchView(forgotView, loginView);
        Platform.runLater(() -> emailField.requestFocus());
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
        String input;

        if (forgotUsingEmail) {
            input = forgotEmailField.getText().trim();
            if (input.isEmpty()) {
                forgotErrorLabel.setText("Please enter your email");
                shakeAnimation(forgotEmailField.getParent());
                return;
            }
        } else {
            String code = forgotCountryCodeField.getText().trim();
            String number = forgotPhoneNumberField.getText().trim();

            if (code.isEmpty() || code.length() != 3) {
                forgotErrorLabel.setText("Country code must be exactly 3 digits (e.g. 970)");
                shakeAnimation(forgotCountryCodeField.getParent());
                return;
            }
            if (number.isEmpty() || number.length() != 9) {
                forgotErrorLabel.setText("Phone number must be exactly 9 digits");
                shakeAnimation(forgotPhoneNumberField.getParent());
                return;
            }
            input = "+" + code + number;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();

        if (newPassword.trim().isEmpty()) {
            forgotErrorLabel.setText("Password cannot be empty or spaces only");
            shakeAnimation(newPasswordField.getParent());
            return;
        }

        if (newPassword.length() < 8) {
            forgotErrorLabel.setText("Password must be at least 8 characters");
            shakeAnimation(newPasswordField.getParent());
            return;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            forgotErrorLabel.setText("Password must contain at least one uppercase letter");
            shakeAnimation(newPasswordField.getParent());
            return;
        }

        if (!newPassword.matches(".*[a-z].*")) {
            forgotErrorLabel.setText("Password must contain at least one lowercase letter");
            shakeAnimation(newPasswordField.getParent());
            return;
        }

        if (!newPassword.matches(".*\\d.*")) {
            forgotErrorLabel.setText("Password must contain at least one number");
            shakeAnimation(newPasswordField.getParent());
            return;
        }

        if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            forgotErrorLabel.setText("Password must contain at least one special character");
            shakeAnimation(newPasswordField.getParent());
            return;
        }

        if (confirmPassword.trim().isEmpty()) {
            forgotErrorLabel.setText("Please confirm your new password");
            shakeAnimation(confirmNewPasswordField.getParent());
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            forgotErrorLabel.setText("Passwords do not match");
            shakeAnimation(confirmNewPasswordField.getParent());
            return;
        }

        setButtonLoading(resetPasswordButton, true);

        pendingResetTarget = input;
        pendingResetPassword = newPassword;

        PauseTransition delay = new PauseTransition(Duration.millis(600));
        delay.setOnFinished(e -> {
            try {
                if (!UserDAO.accountExists(input)) {
                    setButtonLoading(resetPasswordButton, false);
                    forgotErrorLabel.setText("No account found with this email or phone");
                    shakeAnimation(forgotUsingEmail ? forgotEmailField.getParent() : forgotPhoneNumberField.getParent());
                    return;
                }

                sendCodeAndShowBoxes(input);

            } catch (Exception ex) {
                setButtonLoading(resetPasswordButton, false);
                forgotErrorLabel.setText("Connection error");
                ex.printStackTrace();
            }
        });
        delay.play();
    }

    private String pendingResetTarget;
    private String pendingResetPassword;
    private Timeline countdownTimeline;
    private int countdownSeconds;

    private void sendCodeAndShowBoxes(String target) {
        String code = VerificationService.generateUniqueCode(target);

        boolean sent;
        if (forgotUsingEmail) {
            sent = VerificationService.sendEmailCode(target, code);
        } else {
            // University project: no real SMS API — show the code in a local simulation dialog.
            String message = VerificationService.simulateSmsCode(target, code);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("SMS Simulation");
            alert.setHeaderText("Verification code (simulated SMS)");
            alert.setContentText(message);
            alert.showAndWait();
            sent = true;
        }

        setButtonLoading(resetPasswordButton, false);

        if (!sent) {
            String detail = com.smartfarm.util.EmailService.getLastError();
            forgotErrorLabel.setText(detail != null && !detail.isBlank()
                    ? detail
                    : "Failed to send verification code. Try again.");
            return;
        }

        forgotErrorLabel.setText("");
        showVerificationBox(forgotUsingEmail ? null : code);
    }

    private void showVerificationBox() {
        showVerificationBox(null);
    }

    private void showVerificationBox(String simulatedPhoneCode) {
        if (forgotUsingEmail) {
            verificationInfoLabel.setText("Enter the 5-digit code sent to your email");
        } else if (simulatedPhoneCode != null && !simulatedPhoneCode.isBlank()) {
            verificationInfoLabel.setText("Simulated SMS code: " + simulatedPhoneCode
                    + "  — enter it below");
        } else {
            verificationInfoLabel.setText("Enter the 5-digit code from the SMS simulation");
        }

        code1.clear(); code2.clear(); code3.clear(); code4.clear(); code5.clear();

        verificationBox.setVisible(true);
        verificationBox.setManaged(true);
        verificationBox.setOpacity(0);
        verificationBox.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(400), verificationBox);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), verificationBox);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();

        Platform.runLater(() -> code1.requestFocus());

        startCountdown();
    }

    private void startCountdown() {
        resendLink.setVisible(false);
        resendLink.setManaged(false);
        countdownLabel.setVisible(true);
        countdownLabel.setManaged(true);

        countdownSeconds = 30;
        countdownLabel.setText("Resend available in " + countdownSeconds + "s");

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdownSeconds--;
            if (countdownSeconds > 0) {
                countdownLabel.setText("Resend available in " + countdownSeconds + "s");
            } else {
                countdownTimeline.stop();
                countdownLabel.setVisible(false);
                countdownLabel.setManaged(false);
                resendLink.setVisible(true);
                resendLink.setManaged(true);
            }
        }));
        countdownTimeline.setCycleCount(30);
        countdownTimeline.play();
    }

    @FXML
    private void handleResendCode() {
        if (pendingResetTarget == null) return;

        resendLink.setDisable(true);

        PauseTransition delay = new PauseTransition(Duration.millis(400));
        delay.setOnFinished(e -> {
            resendLink.setDisable(false);
            sendCodeAndShowBoxes(pendingResetTarget);
        });
        delay.play();
    }

    @FXML
    private void handleConfirmCode() {
        String enteredCode = code1.getText().trim() + code2.getText().trim() + code3.getText().trim()
                + code4.getText().trim() + code5.getText().trim();

        if (enteredCode.length() != 5) {
            forgotErrorLabel.setText("Please enter all 5 digits");
            shakeAnimation(verificationBox);
            return;
        }

        boolean valid = VerificationService.verifyCode(pendingResetTarget, enteredCode);

        if (!valid) {
            forgotErrorLabel.setText("Invalid or expired code");
            shakeAnimation(verificationBox);
            code1.clear(); code2.clear(); code3.clear(); code4.clear(); code5.clear();
            code1.requestFocus();
            return;
        }

        setButtonLoading(confirmCodeButton, true);

        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> {
            try {
                boolean updated = UserDAO.resetPassword(pendingResetTarget, pendingResetPassword);
                setButtonLoading(confirmCodeButton, false);

                if (!updated) {
                    forgotErrorLabel.setText("Something went wrong. Try again.");
                    return;
                }

                forgotErrorLabel.setStyle("-fx-text-fill: #2E7D32;");
                forgotErrorLabel.setText("\u2714 Password reset successfully!");

                if (countdownTimeline != null) countdownTimeline.stop();

                PauseTransition back = new PauseTransition(Duration.millis(1500));
                back.setOnFinished(ev -> {
                    forgotErrorLabel.setText("");
                    forgotErrorLabel.setStyle("");
                    forgotEmailField.clear();
                    forgotCountryCodeField.clear();
                    forgotPhoneNumberField.clear();
                    newPasswordField.clear();
                    confirmNewPasswordField.clear();
                    verificationBox.setVisible(false);
                    verificationBox.setManaged(false);
                    pendingResetTarget = null;
                    pendingResetPassword = null;
                    handleBackToLogin();
                });
                back.play();

            } catch (Exception ex) {
                setButtonLoading(confirmCodeButton, false);
                forgotErrorLabel.setText("Connection error");
                ex.printStackTrace();
            }
        });
        delay.play();
    }

    private void setupCodeBoxAutoAdvance() {
        TextField[] boxes = { code1, code2, code3, code4, code5 };

        for (int i = 0; i < boxes.length; i++) {
            final int index = i;
            TextField current = boxes[i];

            current.textProperty().addListener((obs, oldVal, newVal) -> {
                String digits = newVal.replaceAll("[^0-9]", "");
                if (digits.length() > 1) digits = digits.substring(digits.length() - 1);
                if (!digits.equals(newVal)) {
                    current.setText(digits);
                    return;
                }
                if (!digits.isEmpty() && index < boxes.length - 1) {
                    boxes[index + 1].requestFocus();
                }
            });

            current.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.BACK_SPACE && current.getText().isEmpty() && index > 0) {
                    boxes[index - 1].requestFocus();
                }
                if (e.getCode() == KeyCode.ENTER) {
                    handleConfirmCode();
                }
            });
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
        SceneSwitcher.setDarkMode(isDarkMode);

        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            if (!rootPane.getStyleClass().contains("dark-mode")) {
                rootPane.getStyleClass().add("dark-mode");
            }
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            rootPane.getStyleClass().remove("dark-mode");
        }
    }
}
