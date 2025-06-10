package dev.andba.trismultiplayergame.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static <T> T  switchScene(String fxmlFile, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/dev/andba/trismultiplayergame/fxml/" + fxmlFile + ".fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
            primaryStage.show();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void switchScene(String fxmlFile) {
        switchScene(fxmlFile, 800, 800);
    }
}
