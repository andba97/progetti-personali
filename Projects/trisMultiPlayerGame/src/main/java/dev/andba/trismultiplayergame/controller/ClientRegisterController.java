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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ClientRegisterController {
    @FXML
    private PasswordField confirmPasswordLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField usernameField;

    private ObjectMapper objectMapper;
    private Client client;

    @FXML
    protected void initialize(){
        objectMapper = new ObjectMapper();
    }

    @FXML
    public void register(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if(username.isBlank()) {
            messageLabel.setText("Per favore inserire un username");
            return;
        }
        else if(password.isBlank()) {
            messageLabel.setText("Per favore inserire una password");
            return;
        }
        else if(confirmPassword.isBlank() || !confirmPassword.equals(password)){
            messageLabel.setText("Per favore confermare la password in modo corretto");
            return;
        }

        User user = new User(username,password);
        ClientOperation operation = new ClientOperation("Register",user);
        try {
            String message = objectMapper.writeValueAsString(operation);
            client.send(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void backToLogin(ActionEvent event) {
        ClientLoginController clientLoginController = SceneManager.switchScene("ClientLogin-view",400,400);
        clientLoginController.initialize(client);
    }

    private void handleMessage(String response) {
        Platform.runLater(() -> {
            switch (response) {
                case "OK":
                    messageLabel.setText("Registrazione effettuato con successo");
                    break;
                case "ERROR4":
                    messageLabel.setText("Username gia presente");
                    break;
                default:
                    messageLabel.setText("Default error");
            }

            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        });
    }

    public void initialize(Client client) {
        this.client = client;
        this.client.setMessageListener(this::handleMessage);
    }

}
