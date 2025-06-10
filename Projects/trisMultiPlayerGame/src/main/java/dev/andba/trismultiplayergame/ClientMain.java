package dev.andba.trismultiplayergame;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.andba.trismultiplayergame.controller.ClientLoginController;
import dev.andba.trismultiplayergame.module.Client;
import dev.andba.trismultiplayergame.module.ClientOperation;
import dev.andba.trismultiplayergame.module.User;
import dev.andba.trismultiplayergame.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientMain extends Application {
    private Client client;

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        client = new Client();
        ClientLoginController controller = SceneManager.switchScene("ClientLogin-view",400,400);
        controller.initialize(client);

        stage.setTitle("Tris");
        stage.setResizable(false);
    }

    @Override
    public void stop() {

        User user = client.getUser();
        ObjectMapper objectMapper = new ObjectMapper();
        if (user != null) {
            ClientOperation operation = new ClientOperation("Logout",user);
            try {
                String message = objectMapper.writeValueAsString(operation);
                client.send(message);
                System.out.println(message);
            } catch (Exception ignored) {}
        }
        client.stop();
    }


    public static void main(String[] args) {
        launch();
    }
}