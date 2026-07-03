package com.smartfarm.controller;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label pageTitle;
    @FXML private Label userName;
    @FXML private StackPane contentArea;
    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;

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

    private boolean isDarkMode = false;
    private Button activeButton;

    @FXML
    public void initialize() {
        activeButton = btnDashboard;
        showDashboard();
    }

private void setActive(Button button, String title) {
    if (activeButton != null) {
        activeButton.getStyleClass().remove("nav-active");
    }
    button.getStyleClass().add("nav-active");
    activeButton = button;
    pageTitle.setText(title);

    javafx.animation.TranslateTransition slide =
        new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), contentArea);
    slide.setFromX(15);
    slide.setToX(0);

    javafx.animation.FadeTransition fade =
        new javafx.animation.FadeTransition(javafx.util.Duration.millis(250), contentArea);
    fade.setFromValue(0.4);
    fade.setToValue(1);

    new javafx.animation.ParallelTransition(slide, fade).play();
}
    @FXML private void showDashboard() {
        setActive(btnDashboard, "Dashboard");
        contentArea.getChildren().setAll(new Label("Dashboard content coming soon..."));
    }

    @FXML private void showFields() {
        setActive(btnFields, "Fields & Crops");
        contentArea.getChildren().setAll(new Label("Fields & Crops content coming soon..."));
    }

    @FXML private void showHarvests() {
        setActive(btnHarvests, "Harvests");
        contentArea.getChildren().setAll(new Label("Harvests content coming soon..."));
    }

    @FXML private void showWorkers() {
        setActive(btnWorkers, "Workers");
        contentArea.getChildren().setAll(new Label("Workers content coming soon..."));
    }

    @FXML private void showTransactions() {
        setActive(btnTransactions, "Transactions");
        contentArea.getChildren().setAll(new Label("Transactions content coming soon..."));
    }

    @FXML private void showStorage() {
        setActive(btnStorage, "Storage");
        contentArea.getChildren().setAll(new Label("Storage content coming soon..."));
    }

    @FXML private void showFertilizers() {
        setActive(btnFertilizers, "Fertilizers");
        contentArea.getChildren().setAll(new Label("Fertilizers content coming soon..."));
    }

    @FXML private void showHistory() {
        setActive(btnHistory, "History");
        contentArea.getChildren().setAll(new Label("History content coming soon..."));
    }

    @FXML private void showReports() {
        setActive(btnReports, "Reports");
        contentArea.getChildren().setAll(new Label("Reports content coming soon..."));
    }

    @FXML private void showSettings() {
        setActive(btnSettings, "Settings");
        contentArea.getChildren().setAll(new Label("Settings content coming soon..."));
    }

    @FXML
    private void handleQuit() {
        System.exit(0);
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