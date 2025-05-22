package dev.andba.todolist;

import dev.andba.todolist.Util.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ToDoListApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.switchScene("ToDoList-view");

        stage.setTitle("To do List!");
        stage.setMinWidth(1000);
        Image icon = new Image(getClass().getResourceAsStream("/img/icon.png"));
        stage.getIcons().add(icon);
    }

    public static void main(String[] args) {
        launch();
    }
}