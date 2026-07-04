package com.smartfarm.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SceneSwitcher {

    private static boolean darkMode = false;

    public static void switchTo(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (darkMode) {
                root.getStyleClass().add("dark-mode");
            }

            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean value) {
        darkMode = value;
    }
}