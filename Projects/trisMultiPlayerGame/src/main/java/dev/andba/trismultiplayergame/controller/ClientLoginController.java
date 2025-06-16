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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Metodo chiamato quando l'utente clicca il pulsante di login.
     */
    @FXML
    void login(ActionEvent event) {
        // Ottieni username e password dai campi di testo
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Controlla se il campo username è vuoto
        if (username.isBlank()) {
            messageLabel.setText("Per favore inserire un username");
            return;
        }
        // Controlla se il campo password è vuoto
        else if (password.isBlank()) {
            messageLabel.setText("Per favore inserire una password");
            return;
        }

        // Crea un oggetto User e un'operazione di login
        User user = new User(username, password);
        ClientOperation operation = new ClientOperation("Login", user);

        try {
            // Serializza l'oggetto operazione in formato JSON e lo invia al server
            String message = objectMapper.writeValueAsString(operation);
            client.send(message);
        } catch (JsonProcessingException e) {
            // Gestione degli errori di serializzazione JSON
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo chiamato quando l'utente clicca il pulsante di registrazione.
     */
    @FXML
    void register(ActionEvent event) {
        // Cambia scena alla vista di registrazione e inizializza il relativo controller
        ClientRegisterController clientRegisterController = SceneManager.switchScene("ClientRegister-view", 400, 400);
        clientRegisterController.initialize(client);
    }

    /**
     * Gestisce la risposta del server ricevuta dopo il login.
     */
    private void handleMessage(String response) {
        // Assicura che il codice venga eseguito sul thread dell'interfaccia grafica (JavaFX)
        Platform.runLater(() -> {
            switch (response) {
                case "OK" -> {
                    // Login riuscito: salva l'utente connesso e passa alla schermata principale
                    String username = usernameField.getText();
                    String password = passwordField.getText();
                    User user = new User(username, password);
                    client.setConnectedUser(user);
                    ClientHubController clientHubController = SceneManager.switchScene("ClientHub-view", 600, 450);
                    clientHubController.initialize(client);
                }
                case "ERROR1" -> messageLabel.setText("Utente non trovato");       // Username non esistente
                case "ERROR2" -> messageLabel.setText("Password errata");         // Password sbagliata
                default -> messageLabel.setText("Errore imprevisto");            // Qualsiasi altro errore
            }
        });
    }

    /**
     * Metodo chiamato per inizializzare il controller con un oggetto Client.
     */
    public void initialize(Client client) {
        this.client = client;
        // Imposta un listener per gestire i messaggi ricevuti dal server
        this.client.setMessageListener(this::handleMessage);
    }
}