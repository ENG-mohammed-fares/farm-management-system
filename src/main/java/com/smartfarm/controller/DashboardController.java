package com.smartfarm.controller;

import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label pageTitle;
    @FXML private Label userName;
    @FXML private StackPane contentArea;
    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;

    @FXML private VBox navButtons;
    @FXML private Region slideHighlight;

    @FXML private Button btnDashboard;
    @FXML private Button btnFields;
    @FXML private Button btnHarvests;
    @FXML private Button btnWorkers;
    @FXML private Button btnTransactions;
    @FXML private Button btnStorage;
    @FXML private Button btnFertilizers;
    @FXML private Button btnHistory;
    @FXML private Button btnReports;
    @FXML private Button btnSettings;
    @FXML private Button btnQuit;

    private boolean isDarkMode = false;
    private Button activeButton;
    private TranslateTransition currentMove;

    @FXML
    public void initialize() {

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(contentArea.widthProperty());
        clip.heightProperty().bind(contentArea.heightProperty());
        contentArea.setClip(clip);


        slideHighlight.minWidthProperty().bind(btnDashboard.widthProperty());
        slideHighlight.prefWidthProperty().bind(btnDashboard.widthProperty());
        slideHighlight.maxWidthProperty().bind(btnDashboard.widthProperty());
        slideHighlight.minHeightProperty().bind(btnDashboard.heightProperty());
        slideHighlight.prefHeightProperty().bind(btnDashboard.heightProperty());
        slideHighlight.maxHeightProperty().bind(btnDashboard.heightProperty());


        navButtons.heightProperty().addListener((o, ov, nv) -> {
            if (activeButton != null) moveHighlight(activeButton, false);
        });
        navButtons.widthProperty().addListener((o, ov, nv) -> {
            if (activeButton != null) moveHighlight(activeButton, false);
        });

        showDashboard();
    }

    private void setActive(Button button, String title) {
        Button previous = activeButton;
        activeButton = button;
        pageTitle.setText(title);


        if (previous != null && previous != button) {
            previous.getStyleClass().remove("nav-active");
        }


        moveHighlight(button, previous != null);
    }

    private void moveHighlight(Button button, boolean animate) {
        double h = button.getHeight();
        if (h <= 0) {

            Platform.runLater(() -> moveHighlight(button, false));
            return;
        }

        double targetY = button.getBoundsInParent().getMinY();

        if (animate) {
            if (currentMove != null) currentMove.stop();
            currentMove = new TranslateTransition(Duration.millis(300), slideHighlight);
            currentMove.setToY(targetY);
            currentMove.setInterpolator(Interpolator.EASE_BOTH);

            currentMove.setOnFinished(e -> ensureActiveClass(button));
            currentMove.play();
        } else {
            slideHighlight.setTranslateY(targetY);
            ensureActiveClass(button);
        }
    }

    private void ensureActiveClass(Button button) {
        if (button == activeButton && !button.getStyleClass().contains("nav-active")) {
            button.getStyleClass().add("nav-active");
        }
    }


    private void setContent(Node content) {
        contentArea.getChildren().setAll(content);

        content.setTranslateX(40);
        content.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), content);
        slide.setFromX(40);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(300), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(slide, fade).play();
    }

    @FXML private void showDashboard() {
        setActive(btnDashboard, "Dashboard");
        setContent(new Label("Dashboard content coming soon..."));
    }

    @FXML private void showFields() {
        setActive(btnFields, "Fields & Crops");
        setContent(new Label("Fields & Crops content coming soon..."));
    }

    @FXML private void showHarvests() {
        setActive(btnHarvests, "Harvests");
        setContent(new Label("Harvests content coming soon..."));
    }

    @FXML private void showWorkers() {
        setActive(btnWorkers, "Workers");
        setContent(new Label("Workers content coming soon..."));
    }

    @FXML private void showTransactions() {
        setActive(btnTransactions, "Transactions");
        setContent(new Label("Transactions content coming soon..."));
    }

    @FXML private void showStorage() {
        setActive(btnStorage, "Storage");
        setContent(new Label("Storage content coming soon..."));
    }

    @FXML private void showFertilizers() {
        setActive(btnFertilizers, "Fertilizers");
        setContent(new Label("Fertilizers content coming soon..."));
    }

    @FXML private void showHistory() {
        setActive(btnHistory, "History");
        setContent(new Label("History content coming soon..."));
    }

    @FXML private void showReports() {
        setActive(btnReports, "Reports");
        setContent(new Label("Reports content coming soon..."));
    }

    @FXML private void showSettings() {
        setActive(btnSettings, "Settings");
        setContent(new Label("Settings content coming soon..."));
    }

    @FXML
    private void handleQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Quit");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("All unsaved changes will be lost.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Scene scene = new Scene(loader.load());

                stage = (Stage) btnQuit.getScene().getWindow();
                stage.setScene(scene);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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