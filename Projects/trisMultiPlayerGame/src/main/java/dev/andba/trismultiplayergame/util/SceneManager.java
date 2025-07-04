package dev.andba.trismultiplayergame.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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
            scene.getStylesheets().add(SceneManager.class.getResource("/css/stile.css").toExternalForm());
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
