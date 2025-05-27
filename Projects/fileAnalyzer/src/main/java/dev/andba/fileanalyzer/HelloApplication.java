package dev.andba.fileanalyzer;

import dev.andba.fileanalyzer.Util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.switchScene("analyzer-view");

        stage.setTitle("Analyzer!");
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch();
    }
}