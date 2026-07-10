package com.smartfarm.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SceneSwitcher {

    private static boolean darkMode = false;
    private static String lastErrorMessage = "";

    public static boolean switchTo(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (darkMode) {
                root.getStyleClass().add("dark-mode");
            }

            stage.getScene().setRoot(root);
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String message = root.getMessage();
            lastErrorMessage = root.getClass().getSimpleName()
                    + (message != null && !message.isBlank() ? ": " + message : "");
            return false;
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean value) {
        darkMode = value;
    }
}
