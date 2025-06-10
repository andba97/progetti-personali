package dev.andba.trismultiplayergame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.andba.trismultiplayergame.module.Client;
import dev.andba.trismultiplayergame.module.ClientOperation;
import dev.andba.trismultiplayergame.module.User;
import dev.andba.trismultiplayergame.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ClientLoginController {

    @FXML
    private Button RegisterButton;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    private Client client;
    private ObjectMapper objectMapper;

    @FXML
    protected void initialize(){
        objectMapper = new ObjectMapper();
    }

    @FXML
    void login(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if(username.isBlank()) {
            messageLabel.setText("Per favore inserire un username");
            return;
        }
        else if(password.isBlank()) {
            messageLabel.setText("Per favore inserire una password");
            return;
        }

        User user = new User(username,password);
        ClientOperation operation = new ClientOperation("Login",user);
        try {
            String message = objectMapper.writeValueAsString(operation);
            client.send(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void register(ActionEvent event) {
        ClientRegisterController clientRegisterController = SceneManager.switchScene("ClientRegister-view",400,400);
        clientRegisterController.initialize(client);
    }

    private void handleMessage(String response) {
        Platform.runLater(() -> {
            switch (response) {
                case "OK" -> {
                    String username = usernameField.getText();
                    String password = passwordField.getText();
                    User user = new User(username, password);
                    client.setConnectedUser(user);
                    ClientHubController clientHubController = SceneManager.switchScene("ClientHub-view", 600, 450);
                    clientHubController.initialize(client);
                }
                case "ERROR1" -> messageLabel.setText("Utente non trovato");
                case "ERROR2" -> messageLabel.setText("Password errata");
                default -> messageLabel.setText("Errore imprevisto");
            }
        });
    }

    public void initialize(Client client) {
        this.client = client;
        this.client.setMessageListener(this::handleMessage);
    }

}
