package dev.andba.trismultiplayergame;

import dev.andba.trismultiplayergame.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.switchScene("Server-view");

        stage.setTitle("ServerTris");
        stage.setMinWidth(1000);
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch();
    }
}
